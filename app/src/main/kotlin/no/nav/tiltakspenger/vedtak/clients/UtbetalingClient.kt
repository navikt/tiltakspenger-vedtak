package no.nav.tiltakspenger.vedtak.clients

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.server.config.ApplicationConfig
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.vedtak.db.objectMapper
import org.slf4j.LoggerFactory

class UtbetalingClient(
    private val config: ApplicationConfig,
    private val client: HttpClient = httpClientWithRetry(timeout = 30L),
    private val utbetalingCredentialsClient: UtbetalingCredentialsClient = UtbetalingCredentialsClient(config),
) : Utbetaling {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun iverksett(behandling: Behandling): String {
        val token = utbetalingCredentialsClient.getToken()
        val res = client.post(iverksettEndpoint) {
            accept(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(behandling.id))
            bearerAuth(token)
        }
        return "IKKE NOE HER ER FERDIG ENDA"
    }
}
