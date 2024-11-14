package no.nav.tiltakspenger.vedtak.clients.datadeling

import arrow.core.Either
import arrow.core.left
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import no.nav.tiltakspenger.datadeling.ports.DatadelingGateway
import no.nav.tiltakspenger.datadeling.ports.FeilVedSendingTilDatadeling
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class DatadelingHttpClient(
    baseUrl: String,
    val getToken: suspend () -> AccessToken,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 1.seconds,
) : DatadelingGateway {
    private val log = KotlinLogging.logger {}

    private val client = java.net.http.HttpClient
        .newBuilder()
        .connectTimeout(connectTimeout.toJavaDuration())
        .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
        .build()

    private val behandlingsUri = URI.create("$baseUrl/behandling")
    private val vedtaksUri = URI.create("$baseUrl/vedtak")

    override suspend fun send(
        rammevedtak: Rammevedtak,
        correlationId: CorrelationId,
    ): Either<FeilVedSendingTilDatadeling, Unit> {
        val jsonPayload = rammevedtak.toDatadelingJson()
        return Either.catch {
            val request = createRequest(jsonPayload, vedtaksUri)
            val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
            val jsonResponse = httpResponse.body()
            val status = httpResponse.statusCode()
            if (status != 200) {
                log.error { "Feil ved kall til tiltakspenger-datadeling. Vedtak ${rammevedtak.id}, saksnummer ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}. Status: $status. uri: $vedtaksUri. Se sikkerlogg for detaljer." }
                sikkerlogg.error { "Feil ved kall til tiltakspenger-datadeling. Vedtak ${rammevedtak.id}, saksnummer ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}. uri: $vedtaksUri. jsonResponse: $jsonResponse. jsonPayload: $jsonPayload." }
                return FeilVedSendingTilDatadeling.left()
            }
            Unit
        }.mapLeft {
            // Either.catch slipper igjennom CancellationException som er ønskelig.
            log.error(it) { "Feil ved kall til tiltakspenger-datadeling. Vedtak ${rammevedtak.id}, saksnummer ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}. uri: $vedtaksUri. Se sikkerlogg for detaljer." }
            sikkerlogg.error(it) { "Feil ved kall til tiltakspenger-datadeling. Vedtak ${rammevedtak.id}, saksnummer ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}, uri: $vedtaksUri, jsonPayload: $jsonPayload" }
            FeilVedSendingTilDatadeling
        }
    }

    override suspend fun send(
        behandling: Førstegangsbehandling,
        correlationId: CorrelationId,
    ): Either<FeilVedSendingTilDatadeling, Unit> {
        val jsonPayload = behandling.toBehandlingJson()
        return Either.catch {
            val request = createRequest(jsonPayload, behandlingsUri)
            val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
            val jsonResponse = httpResponse.body()
            val status = httpResponse.statusCode()
            if (status != 200) {
                log.error { "Feil ved kall til tiltakspenger-datadeling. Behandling ${behandling.id}, saksnummer ${behandling.saksnummer}, sakId: ${behandling.sakId}. Status: $status. uri: $behandlingsUri. Se sikkerlogg for detaljer." }
                sikkerlogg.error { "Feil ved kall til tiltakspenger-datadeling. Behandling ${behandling.id}, saksnummer ${behandling.saksnummer}, sakId: ${behandling.sakId}. uri: $behandlingsUri. jsonResponse: $jsonResponse. jsonPayload: $jsonPayload." }
                return FeilVedSendingTilDatadeling.left()
            }
            Unit
        }.mapLeft {
            // Either.catch slipper igjennom CancellationException som er ønskelig.
            log.error(it) { "Feil ved kall til pdfgen. Vedtak ${behandling.id}, saksnummer ${behandling.saksnummer}, sakId: ${behandling.sakId}. Se sikkerlogg for detaljer." }
            sikkerlogg.error(it) { "Feil ved kall til pdfgen. Vedtak ${behandling.id}, saksnummer ${behandling.saksnummer}, sakId: ${behandling.sakId}. jsonPayload: $jsonPayload, uri: $behandlingsUri" }
            FeilVedSendingTilDatadeling
        }
    }

    private suspend fun createRequest(
        jsonPayload: String,
        uri: URI,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Authorization", "Bearer ${getToken().token}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()
}