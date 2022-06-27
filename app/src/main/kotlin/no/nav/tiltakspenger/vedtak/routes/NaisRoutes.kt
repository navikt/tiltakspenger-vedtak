package no.nav.tiltakspenger.vedtak.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}
private const val IS_ALIVE_PATH = "/isAlive"
private const val IS_READY_PATH = "/isReady"

internal fun Application.naisRoutes() {
    routing {
        route(IS_ALIVE_PATH) {
            get {
                call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain, status = HttpStatusCode.OK)
            }
        }.also { LOG.info { "setting up endpoint /isAlive" } }
        route(IS_READY_PATH) {
            get {
                call.respondText(text = "READY", contentType = ContentType.Text.Plain)
            }
        }.also { LOG.info { "setting up endpoint /isReady" } }
    }
}
