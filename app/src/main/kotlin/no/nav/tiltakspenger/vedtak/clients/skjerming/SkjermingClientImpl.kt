package no.nav.tiltakspenger.vedtak.clients.skjerming

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper

class SkjermingClientImpl(
    private val skjermingConfig: Configuration.ClientConfig = Configuration.skjermingClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine,
    ) {},
) : SkjermingClient {
    companion object {
        const val navCallIdHeader = "Nav-Call-Id"
    }

    override suspend fun erSkjermetPerson(fnr: Fnr): Boolean {
        val httpResponse = httpClient.preparePost("${skjermingConfig.baseUrl}/skjermet") {
            header(navCallIdHeader, navCallIdHeader)
            bearerAuth(getToken())
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(SkjermetDataRequestDTO(fnr.verdi))
        }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.call.response.body()
            else -> throw RuntimeException("error (responseCode=${httpResponse.status.value}) from Skjerming")
        }
    }

    private data class SkjermetDataRequestDTO(val personident: String)
}
