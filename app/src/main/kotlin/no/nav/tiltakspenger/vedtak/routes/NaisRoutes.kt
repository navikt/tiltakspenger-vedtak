package no.nav.tiltakspenger.vedtak.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}
private const val IS_ALIVE_PATH = "/isalive"
private const val IS_READY_PATH = "/isready"

internal fun Route.naisRoutes() {
    route(IS_ALIVE_PATH) {
        get {
            call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain, status = HttpStatusCode.OK)
        }
    }.also { LOG.info { "setting up endpoint /isalive" } }
    route(IS_READY_PATH) {
        get {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        }
    }.also { LOG.info { "setting up endpoint /isready" } }
}
