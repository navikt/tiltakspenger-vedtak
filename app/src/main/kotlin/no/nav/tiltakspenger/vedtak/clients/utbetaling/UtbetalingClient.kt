package no.nav.tiltakspenger.vedtak.clients.utbetaling

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper

val securelog = KotlinLogging.logger("tjenestekall")

data class UtbetalingReqBody(
    val testFelt: String,
)

class UtbetalingClient(
    private val config: Configuration.ClientConfig = Configuration.utbetalingClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine,
    ),
) : Utbetaling {
    companion object {
        const val navCallIdHeader = "Nav-Call-Id"
    }

    override suspend fun iverksett(behandling: Behandling): String {
        val httpResponse =
            httpClient.post("${config.baseUrl}/utbetaling") {
                header(navCallIdHeader, "tiltakspenger-vedtak")
                bearerAuth(getToken())
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    UtbetalingReqBody(
                        testFelt = "test",
                    ),
                )
            }

        return when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.call.response.body()
            else -> throw RuntimeException("error (responseCode=${httpResponse.status.value}) fra Komet")
        }
    }
}
