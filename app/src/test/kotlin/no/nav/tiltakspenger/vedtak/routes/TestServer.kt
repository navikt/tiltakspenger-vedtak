package no.nav.tiltakspenger.vedtak.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.tiltakspenger.vedtak.repository.søker.InMemorySøkerRepository
import no.nav.tiltakspenger.vedtak.routes.søker.søkerRoutes
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        apply { vedtakTestApi() }
    }.start(wait = true)
}

internal fun vedtakTestApi(
//    søkerRepository: SøkerRepository = PostgresSøkerRepository(
//        SøknadDAO()
//    )
): Application.() -> Unit {
    return {
        jacksonSerialization()
        routing {
            søkerRoutes(
                innloggetBrukerProvider = InnloggetBrukerProvider(),
                søkerService = SøkerServiceImpl(
                    søkerRepository = InMemorySøkerRepository(),
                ),
            )
        }
    }
}

suspend fun ApplicationTestBuilder.defaultRequest(
    method: HttpMethod,
    uri: String,
    setup: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    return this.client.request(uri) {
        this.method = method
        this.headers {
            append(HttpHeaders.XCorrelationId, "DEFAULT_CALL_ID")
            append(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        setup()
    }
}
