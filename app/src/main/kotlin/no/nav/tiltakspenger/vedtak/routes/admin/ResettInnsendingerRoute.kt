package no.nav.tiltakspenger.vedtak.routes.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
