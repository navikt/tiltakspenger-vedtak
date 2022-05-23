package no.nav.tiltakspenger.vedtak

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.routes.naisRoutes
import no.nav.tiltakspenger.vedtak.routes.sak.sakRoutes
import java.time.Clock

private val LOG = KotlinLogging.logger {}
const val PORT = 8080

fun main() {
    LOG.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }

    val server = embeddedServer(Netty, PORT) {
        tiltakspenger()
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
        }
    )
}

fun Application.tiltakspenger(
    clock: Clock = Clock.systemUTC(),
) {
    naisRoutes()

    routing {
        sakRoutes()
    }
}
