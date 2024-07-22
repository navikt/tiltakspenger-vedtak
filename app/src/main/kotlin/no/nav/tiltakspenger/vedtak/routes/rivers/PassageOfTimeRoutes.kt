package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

val dayHasBegunRoute = "/rivers/passageoftime/dayhasbegun"

fun Route.passageOfTimeRoutes() {
    post(dayHasBegunRoute) {
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
