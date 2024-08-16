package no.nav.tiltakspenger.vedtak.clients.meldekort

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
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
        vedtak: Vedtak,
        correlationId: CorrelationId,
    ): Either<KunneIkkeSendeMeldekortGrunnlag, Boolean> =
        withContext(Dispatchers.IO) {
            Either
                .catch {
                    val request = createRequest(vedtak, correlationId)

                    val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                    val body = httpResponse.body()
                    if (httpResponse.isSuccess()) {
                        Either
                            .catch {
                                body.lowercase().toBooleanStrict()
                            }.mapLeft {
                                KunneIkkeSendeMeldekortGrunnlag.DeserializationException(it)
                            }
                    } else {
                        KunneIkkeSendeMeldekortGrunnlag.Ikke2xx(status = httpResponse.statusCode(), body = body).left()
                    }
                }.mapLeft {
                    // Either.catch slipper igjennom CancellationException som er ønskelig.
                    KunneIkkeSendeMeldekortGrunnlag.NetworkError(it)
                }.flatten()
        }

    private fun createRequest(
        vedtak: Vedtak,
        correlationId: CorrelationId,
    ): HttpRequest? =
        HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Authorization", "Bearer $getSystemToken()")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header(NAV_CALL_ID_HEADER, correlationId.value)
            .POST(HttpRequest.BodyPublishers.ofString(MeldekortGrunnlagDTOMapper.toJson(vedtak)))
            .build()
}

private fun <T> HttpResponse<T>.isSuccess(): Boolean = this.statusCode() in 200..299
