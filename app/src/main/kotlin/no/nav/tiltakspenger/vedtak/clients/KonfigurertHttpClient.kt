package no.nav.tiltakspenger.vedtak.clients

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.jackson.jackson
import mu.KotlinLogging
import java.time.Duration


private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

private const val SIXTY_SECONDS = 60L
fun httpClientCIO(timeout: Long = SIXTY_SECONDS) = HttpClient(CIO).config(timeout)
fun httpClientGeneric(engine: HttpClientEngine, timeout: Long = SIXTY_SECONDS) = HttpClient(engine).config(timeout)
fun httpClientWithRetry(timeout: Long = SIXTY_SECONDS) = httpClientCIO(timeout).also { httpClient ->
    httpClient.config {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = true)
            constantDelay(100, 0, false)
        }
    }
}

private fun HttpClient.config(timeout: Long) = this.config {
    install(ContentNegotiation) {
        jackson {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                },
            )
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(HttpTimeout) {
        connectTimeoutMillis = Duration.ofSeconds(timeout).toMillis()
        requestTimeoutMillis = Duration.ofSeconds(timeout).toMillis()
        socketTimeoutMillis = Duration.ofSeconds(timeout).toMillis()
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                LOG.info("HttpClient detaljer logget til securelog")
                SECURELOG.info(message)
            }
        }
        level = LogLevel.INFO
    }
    expectSuccess = true
}

//fun defaultObjectMapper(): ObjectMapper = JsonMapper.builder()
//    .addModule(KotlinModule.Builder().build())
//    .addModule(JavaTimeModule())
//    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//    .build()
