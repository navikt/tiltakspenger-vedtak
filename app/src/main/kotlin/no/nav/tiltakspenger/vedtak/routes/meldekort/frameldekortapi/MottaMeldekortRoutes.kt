package no.nav.tiltakspenger.vedtak.routes.meldekort.frameldekortapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

fun Route.mottaMeldekortRoutes() {
    val logger = KotlinLogging.logger { }

    post("/meldekort/motta") {
        logger.debug { "Mottatt post-request på /meldekort/motta" }
        call.respond(status = HttpStatusCode.OK, "")
    }
}
