package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.routes.parameter
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

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val begrunnelse = call.receive<BegrunnelseDTO>().begrunnelse

        behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler, begrunnelse)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/godkjenn/{behandlingId}") {
        SECURELOG.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.iverksett(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
