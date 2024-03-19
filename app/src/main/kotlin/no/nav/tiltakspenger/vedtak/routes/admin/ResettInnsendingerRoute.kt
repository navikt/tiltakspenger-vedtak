package no.nav.tiltakspenger.vedtak.routes.admin

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.tiltakspenger.innsending.service.InnsendingAdminService

internal const val resettFeiletOgStoppetInnsendinger = "/innsendinger/resett/feiletogstoppet"
internal const val feiletOgStoppetInnsendinger = "/innsendinger/feiletogstoppet"

fun Route.resettInnsendingerRoute(
    innsendingAdminService: InnsendingAdminService,
) {
    get(feiletOgStoppetInnsendinger) {
        call.respond(innsendingAdminService.hentInnsendingerSomHarFeiletEllerStoppetOpp())
    }
    post(resettFeiletOgStoppetInnsendinger) {
        innsendingAdminService.resettInnsendingerSomHarFeiletEllerStoppetOpp()
        call.respond(message = AdminMessage("ok"), status = HttpStatusCode.OK)
    }
}

data class AdminMessage(val status: String)
