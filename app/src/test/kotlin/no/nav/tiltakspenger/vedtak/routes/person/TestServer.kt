package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        vedtakTestApi()()
    }.start(wait = true)
}

internal fun vedtakTestApi(): Application.() -> Unit {
    return {
        jacksonSerialization()
        routing {
            personRoutes(InnloggetBrukerProvider())
        }
    }
}
