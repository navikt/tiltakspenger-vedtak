package no.nav.tiltakspenger.vedtak.routes

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.append
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.tiltakspenger.vedtak.repository.søker.InMemorySøkerRepository
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
import no.nav.tiltakspenger.vedtak.service.PersonServiceImpl
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
            personRoutes(
                innloggetBrukerProvider = InnloggetBrukerProvider(),
                personService = PersonServiceImpl(
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
