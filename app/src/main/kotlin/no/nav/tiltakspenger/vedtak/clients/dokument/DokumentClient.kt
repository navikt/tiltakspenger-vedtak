package no.nav.tiltakspenger.vedtak.clients.dokument

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
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.meldekort.ports.JoarkResponse
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient
import no.nav.tiltakspenger.vedtak.db.objectMapper

private val LOG = KotlinLogging.logger {}

class DokumentClient(
    private val baseUrl: String,
    private val getToken: suspend () -> AccessToken,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient =
        defaultHttpClient(
            objectMapper = objectMapper,
            engine = engine,
        ),
) : DokumentGateway {
    companion object {
        const val NAV_CALL_ID_HEADER = "Nav-Call-Id"
    }

    override suspend fun sendMeldekortTilDokument(
        vedtak: Utbetalingsvedtak,
        correlationId: CorrelationId,
    ): JoarkResponse {
        LOG.info { "Request motatt for å sende meldekort til tiltakspenger-dokument på $baseUrl/meldekort/arkivmeldekort" }

        val httpResponse =
            httpClient.post("$baseUrl/meldekort/arkivmeldekort") {
                header(NAV_CALL_ID_HEADER, correlationId)
                bearerAuth(getToken().value)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    mapMeldekortDTOTilDokumentDTO(vedtak),
                )
            }

        return when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.call.response.body()
            else -> throw RuntimeException("error (responseCode=${httpResponse.status.value}) fra Dokument")
        }
    }
}
