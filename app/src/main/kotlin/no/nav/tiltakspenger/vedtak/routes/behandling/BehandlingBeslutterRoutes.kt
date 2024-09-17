package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

data class BegrunnelseDTO(
    val begrunnelse: String,
)

fun Route.behandlingBeslutterRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    auditService: AuditService,
) {
    post("$BEHANDLING_PATH/sendtilbake/{behandlingId}") {
        sikkerlogg.debug("Mottatt request. $BEHANDLING_PATH/ send tilbake til saksbehandler")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val begrunnelse = call.receive<BegrunnelseDTO>().begrunnelse

        behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler, begrunnelse)

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Beslutter underkjenner behandling",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$BEHANDLING_PATH/godkjenn/{behandlingId}") {
        sikkerlogg.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.iverksett(behandlingId, saksbehandler)

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Beslutter godkjenner behandlingen",
            callId = call.callId,
        )

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
