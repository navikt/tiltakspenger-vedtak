package no.nav.tiltakspenger.vedtak.clients.utbetaling

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.KunneIkkeUtbetale
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val log = KotlinLogging.logger {}

/**
 * https://navikt.github.io/utsjekk-docs/
 */
class UtbetalingHttpClient(
    baseUrl: String,
    private val getToken: suspend () -> AccessToken,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 1.seconds,
) : UtbetalingGateway {

    private val client =
        HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()

    private val uri = URI.create("$baseUrl/api/iverksetting/v2")

    override suspend fun iverksett(
        vedtak: Utbetalingsvedtak,
        forrigeUtbetalingJson: String?,
        correlationId: CorrelationId,
    ): Either<KunneIkkeUtbetale, SendtUtbetaling> =
        withContext(Dispatchers.IO) {
            Either
                .catch {
                    val jsonPayload = vedtak.toDTO(forrigeUtbetalingJson)
                    val request = createRequest(correlationId, jsonPayload)

                    val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                    val jsonResponse = httpResponse.body()
                    mapStatus(
                        status = httpResponse.statusCode(),
                        vedtak = vedtak,
                        request = jsonPayload,
                        response = jsonResponse,
                    )
                }.mapLeft {
                    // Either.catch slipper igjennom CancellationException som er ønskelig.
                    log.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ukjent feil ved utsjekk for utbetalingsvedtak ${vedtak.id}. Saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}" }
                    sikkerlogg.error(it) { "Ukjent feil ved utsjekk for utbetalingsvedtak ${vedtak.id}. Saksnummer ${vedtak.saksnummer}, sakId: ${vedtak.sakId}" }
                    KunneIkkeUtbetale
                }.flatten()
        }

    private suspend fun createRequest(
        correlationId: CorrelationId,
        jsonPayload: String,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Authorization", "Bearer ${getToken().value}")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            // Dette er kun for vår del, open telemetry vil kunne være et alternativ. Slack tråd: https://nav-it.slack.com/archives/C06SJTR2X3L/p1724072054018589
            .header("Nav-Call-Id", correlationId.value)
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()
}

private fun mapStatus(
    status: Int,
    vedtak: Utbetalingsvedtak,
    request: String,
    response: String,
): Either<KunneIkkeUtbetale, SendtUtbetaling> {
    when (status) {
        202 -> {
            log.info(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "202 Accepted fra helved utsjekk for, utbetalingsvedtak ${vedtak.id}. Response: $response. Se sikkerlogg for mer kontekst."
            }
            sikkerlogg.info(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "202 Accepted fra helved utsjekk for, utbetalingsvedtak ${vedtak.id}. Response: $response. Request = $request"
            }
            return SendtUtbetaling(
                request = request,
                response = response,
            ).right()
        }

        400 -> {
            log.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "400 Bad Request fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Response: $response. Se sikkerlogg for mer kontekst."
            }
            sikkerlogg.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "400 Bad Request fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Response: $response. Request = $request"
            }
            return KunneIkkeUtbetale.left()
        }

        403 -> {
            log.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "403 Forbidden fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Response: $response. Se sikkerlogg for mer kontekst."
            }
            sikkerlogg.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "403 Forbidden fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Response: $response. Request = $request"
            }
            return KunneIkkeUtbetale.left()
        }

        409 -> {
            log.info(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "409 Conflict fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Vi antar vi har sendt samme melding tidligere og behandler denne på samme måte som 202 Response: $response. Se sikkerlogg for mer kontekst."
            }
            sikkerlogg.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "409 Conflict fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Vi antar vi har sendt samme melding tidligere og behandler denne på samme måte som 202 Response: $response. Request = $request"
            }
            return SendtUtbetaling(
                request = request,
                response = response,
            ).right()
        }

        else -> {
            log.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "Ukjent feil fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Statuskode: $status, response: $response. Se sikkerlogg for mer kontekst."
            }
            sikkerlogg.error(RuntimeException("Trigger stacktrace for enklere debug.")) {
                "Ukjent feil fra helved utsjekk, for utbetalingsvedtak ${vedtak.id}. Denne vil bli prøvd på nytt. Statuskode: $status, response: $response. Request = $request"
            }
            return KunneIkkeUtbetale.left()
        }
    }
}
