package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

const val personopplysningerPath = "/rivers/personopplysninger"

private val LOG = KotlinLogging.logger {}

// TODO jah: Denne kan slettes når vi tar ned RnR.
fun Route.personopplysningerRoutes() {
    post(personopplysningerPath) {
        LOG.error { "Vi har mottatt uøsnket personopplysninger fra river. Vi skal ikke lenger motta personopplysninger via rapids and rivers, men heller hente synkront." }
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
