package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class BegrunnelseDTO(
    val begrunnelse: String,
)

fun Route.behandlingBeslutterRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    post("$behandlingPath/sendtilbake/{behandlingId}") {
        SECURELOG.debug("Mottatt request. $behandlingPath/ send tilbake til saksbehandler")

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val isAdmin = saksbehandler.roller.contains(Rolle.ADMINISTRATOR)

        check(saksbehandler.roller.contains(Rolle.BESLUTTER) || isAdmin) { "Saksbehandler må være beslutter eller administrator" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Fant ingen behandlingId i body", status = HttpStatusCode.NotFound)

        val begrunnelse = call.receive<BegrunnelseDTO>().begrunnelse

        behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler.navIdent, begrunnelse, isAdmin)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/godkjenn/{behandlingId}") {
        SECURELOG.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.iverksett(behandlingId, saksbehandler.navIdent)
        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
