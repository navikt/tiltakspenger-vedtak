package no.nav.tiltakspenger.vedtak.clients.pdfgen

import arrow.core.Either
import arrow.core.left
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.PdfA
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.KunneIkkeGenererePdf
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Har ansvar for å konvertere domene til JSON som sendes til https://github.com/navikt/tiltakspenger-pdfgen for å generere PDF.
 *
 * timeout er satt til 6 sekunder siden pdfgen bruker lang tid første gang den genererer en pdf (nesten 5 sekunder). Etter det tar det 1-2 sekunder
 */
internal class PdfgenHttpClient(
    baseUrl: String,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 6.seconds,
) : GenererVedtaksbrevGateway, GenererMeldekortPdfGateway {

    private val log = KotlinLogging.logger {}

    private val client =
        java.net.http.HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
            .build()

    private val vedtakInnvilgelseUri = URI.create("$baseUrl/api/v1/genpdf/tpts/vedtakInnvilgelse")

    override suspend fun genererVedtaksbrev(
        vedtak: Rammevedtak,
        hentBrukersNavn: suspend (Fnr) -> Navn,
        hentSaksbehandlersNavn: suspend (String) -> String,
    ): Either<KunneIkkeGenererePdf, PdfOgJson> {
        return withContext(Dispatchers.IO) {
            val jsonPayload = vedtak.tobrevDTO(hentBrukersNavn, hentSaksbehandlersNavn)
            Either.catch {
                val request = createPdfgenRequest(jsonPayload, vedtakInnvilgelseUri)
                val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).await()
                val jsonResponse = httpResponse.body()
                val status = httpResponse.statusCode()
                if (status != 200) {
                    log.error { "Feil ved kall til pdfgen. Vedtak ${vedtak.id}, saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}. Status: $status. uri: $vedtakInnvilgelseUri. Se sikkerlogg for detaljer." }
                    sikkerlogg.error { "Feil ved kall til pdfgen. Vedtak ${vedtak.id}, saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}. uri: $vedtakInnvilgelseUri. jsonResponse: $jsonResponse. jsonPayload: $jsonPayload." }
                    return@withContext KunneIkkeGenererePdf.left()
                }
                PdfOgJson(PdfA(jsonResponse), jsonPayload)
            }.mapLeft {
                // Either.catch slipper igjennom CancellationException som er ønskelig.
                log.error(it) { "Feil ved kall til pdfgen. Vedtak ${vedtak.id}, saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}. Se sikkerlogg for detaljer." }
                sikkerlogg.error(it) { "Feil ved kall til pdfgen. Vedtak ${vedtak.id}, saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}. jsonPayload: $jsonPayload, uri: $vedtakInnvilgelseUri" }
                KunneIkkeGenererePdf
            }
        }
    }

    override suspend fun genererMeldekortPdf(
        meldekort: Meldekort.UtfyltMeldekort,
        hentBrukersNavn: suspend (Fnr) -> Navn,
    ): PdfOgJson {
        val data = meldekort.toPdf(hentBrukersNavn)
        return genererPdfFraJson(data)
    }

    private fun createPdfgenRequest(
        jsonPayload: String,
        uri: URI,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Accept", "application/pdf")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()
}

private suspend fun genererPdfFraJson(jsonNode: JsonNode): PdfOgJson {
    return withContext(Dispatchers.IO) {
        PDDocument().use { document ->
            val margin = 50f
            val linjeAvstand = 15f
            val sideStørrelse = PDPage().mediaBox.height - margin * 3f
            val linjerPerSide = (sideStørrelse / linjeAvstand).toInt()
            val peneLinjer = jsonNode.toPrettyString().split("\n")
            peneLinjer.chunked(linjerPerSide).forEach { linjer ->
                val side = PDPage()
                document.addPage(side)
                PDPageContentStream(document, side).use { contentStream ->
                    contentStream.setFont(PDType1Font(FontName.HELVETICA), 12f)
                    contentStream.beginText()
                    contentStream.newLineAtOffset(margin, 700f)
                    linjer.forEach { linje ->
                        contentStream.showText(linje)
                        contentStream.newLineAtOffset(0f, -linjeAvstand)
                    }
                    contentStream.endText()
                }
            }

            val byteArray: ByteArray = ByteArrayOutputStream().use { byteArrayOutputStream ->
                document.save(byteArrayOutputStream, CompressParameters.NO_COMPRESSION)
                byteArrayOutputStream.toByteArray()
            }
            PdfOgJson(PdfA(byteArray), jsonNode.toString())
        }
    }
}
