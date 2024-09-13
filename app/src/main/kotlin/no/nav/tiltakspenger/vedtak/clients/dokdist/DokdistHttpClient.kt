package no.nav.tiltakspenger.vedtak.clients.dokdist

import arrow.core.Either
import arrow.core.flatten
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.distribusjon.ports.DokdistGateway
import no.nav.tiltakspenger.distribusjon.ports.KunneIkkeDistribuereDokument
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class DokdistHttpClient(
    baseUrl: String,
    private val getToken: suspend () -> AccessToken,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 1.seconds,
) : DokdistGateway {

    private val log = KotlinLogging.logger {}

    private val client =
        java.net.http.HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
            .build()

    private val uri = URI.create("$baseUrl/rest/v1/distribuerjournalpost")

    override suspend fun distribuerDokument(
        journalpostId: JournalpostId,
        correlationId: CorrelationId,
    ): Either<KunneIkkeDistribuereDokument, DistribusjonId> {
        return withContext(Dispatchers.IO) {
            val jsonPayload = journalpostId.toDokdistRequest()
            Either
                .catch {
                    val request = createRequest(jsonPayload, correlationId)
                    val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                    httpResponse.body().dokdistResponseToDomain(log)
                }.mapLeft {
                    // Either.catch slipper igjennom CancellationException som er ønskelig.
                    log.error(it) { "Feil ved kall til dokdist. journalpostId: $journalpostId. Se sikkerlogg for detaljer." }
                    sikkerlogg.error(it) { "Feil ved kall til dokdist. journalpostId: $journalpostId. . jsonPayload: $jsonPayload, uri: $uri" }
                    KunneIkkeDistribuereDokument
                }.flatten()
        }
    }

    private suspend fun createRequest(
        jsonPayload: String,
        correlationId: CorrelationId,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${getToken().value}")
            .header("Nav-CallId", correlationId.value)
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()
}
