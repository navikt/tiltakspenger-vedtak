package no.nav.tiltakspenger.vedtak.clients.meldekort

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.KunneIkkeSendeMeldekortGrunnlag
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortgrunnlagGateway
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class MeldekortGrunnlagHttpClient(
    baseUrl: String,
    private val getSystemToken: suspend () -> AccessToken,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 1.seconds,
) : MeldekortgrunnlagGateway {
    private val client =
        HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()

    private val uri = URI.create("$baseUrl/meldekort/grunnlag")

    companion object {
        const val NAV_CALL_ID_HEADER = "Nav-Call-Id"
    }

    override suspend fun sendMeldekortgrunnlag(
        vedtak: Rammevedtak,
        correlationId: CorrelationId,
    ): Either<KunneIkkeSendeMeldekortGrunnlag, Unit> =
        withContext(Dispatchers.IO) {
            Either
                .catch {
                    val request = createRequest(vedtak, correlationId)

                    val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                    if (httpResponse.isSuccess()) {
                        Unit.right()
                    } else {
                        val status = httpResponse.statusCode()
                        LOG.error(RuntimeException("Trigger exception for debugging")) {
                            "Feil ved sending av vedtak ${vedtak.id} til tiltakspenger-meldekort-api. Status: $status. Se securelog for context."
                        }
                        SECURELOG.error {
                            "Feil ved sending av vedtak ${vedtak.id} til tiltakspenger-meldekort-api. Status: $status."
                        }
                        KunneIkkeSendeMeldekortGrunnlag.Ikke2xx(status = status).left()
                    }
                }.mapLeft {
                    // Either.catch slipper igjennom CancellationException som er ønskelig.
                    LOG.error(RuntimeException("Trigger exception for stacktrace")) {
                        "Nettverksfeil ved sending av vedtak ${vedtak.id} til tiltakspenger-meldekort-api"
                    }
                    SECURELOG.error(it) { "Nettverksfeil ved sending av vedtak ${vedtak.id} til tiltakspenger-meldekort-api" }
                    KunneIkkeSendeMeldekortGrunnlag.NetworkError(it)
                }.flatten()
        }

    private suspend fun createRequest(
        vedtak: Rammevedtak,
        correlationId: CorrelationId,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Authorization", "Bearer ${getSystemToken().value}")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header(NAV_CALL_ID_HEADER, correlationId.value)
            .POST(HttpRequest.BodyPublishers.ofString(MeldekortGrunnlagDTOMapper.toJson(vedtak)))
            .build()
}

private fun <T> HttpResponse<T>.isSuccess(): Boolean = this.statusCode() in 200..299
