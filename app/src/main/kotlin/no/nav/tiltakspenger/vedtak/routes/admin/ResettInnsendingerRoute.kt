package no.nav.tiltakspenger.vedtak.routes.admin

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService

internal const val resettFeiletOgStoppetInnsendinger = "/innsendinger/resett/feiletogstoppet"

fun Route.resettInnsendingerRoute(
    innsendingAdminService: InnsendingAdminService
) {
    post(resettFeiletOgStoppetInnsendinger) {
        innsendingAdminService.resettInnsendingerSomHarFeiletEllerStoppetOpp()
        call.respond(message = "Ok", status = HttpStatusCode.OK)
    }
}
