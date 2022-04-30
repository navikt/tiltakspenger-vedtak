package no.nav.tiltakspenger.vedtak

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}
const val PORT = 8080

fun main() {
    LOG.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }

    val server = embeddedServer(Netty, PORT) {
        routing {
            route("/isAlive") {
                get {
                    call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain, status = HttpStatusCode.OK)
                }
            }.also { LOG.info { "setting up endpoint /isAlive" } }
            route("/isReady") {
                get {
                    call.respondText(text = "READY", contentType = ContentType.Text.Plain)
                }
            }.also { LOG.info { "setting up endpoint /isReady" } }
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
        }
    )
}
