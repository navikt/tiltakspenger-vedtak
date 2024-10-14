package no.nav.tiltakspenger.vedtak.routes

import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/** Disse skal være helt åpne. */
fun Route.healthRoutes() {
    get("/isalive") {
        call.respondText("ALIVE")
    }

    get("/isready") {
        call.respondText("READY")
    }
}
