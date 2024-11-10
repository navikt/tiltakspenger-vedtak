package no.nav.tiltakspenger.vedtak.clients

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.json.objectMapper
import java.time.Duration

private val LOG = KotlinLogging.logger {}
private const val SIXTY_SECONDS = 60L

// engine skal brukes primært i test-øyemed, når man sender med MockEngine.
// Forøvrig kan man la den være null.
fun defaultHttpClient(
    engine: HttpClientEngine? = null,
    configBlock: HttpClientConfig<*>.() -> Unit = {},
    engineConfigBlock: CIOEngineConfig.() -> Unit = {},
) = engine?.let {
    HttpClient(engine) {
        apply(defaultSetup())
        apply(configBlock)
    }
} ?: HttpClient(CIO) {
    apply(defaultSetup())
    apply(configBlock)
    engine(engineConfigBlock)
}

private fun defaultSetup(): HttpClientConfig<*>.() -> Unit =
    {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
        }

        this.install(Logging) {
            logger =
                object : Logger {
                    override fun log(message: String) {
                        LOG.info("HttpClient detaljer logget til securelog")
                        sikkerlogg.info(message)
                    }
                }
            level = LogLevel.INFO
        }
        this.expectSuccess = true
    }
