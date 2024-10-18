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
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
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
    val logger = KotlinLogging.logger {}
    post("$BEHANDLING_PATH/sendtilbake/{behandlingId}") {
        logger.debug("Mottatt post-request på '$BEHANDLING_PATH/sendtilbake/{behandlingId}' - sender behandling tilbake til saksbehandler")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val begrunnelse = call.receive<BegrunnelseDTO>().begrunnelse

        behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler, begrunnelse, correlationId = call.correlationId())

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Beslutter underkjenner behandling",
            correlationId = call.correlationId(),
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$BEHANDLING_PATH/godkjenn/{behandlingId}") {
        logger.debug { "Mottatt post-request på '$BEHANDLING_PATH/godkjenn/{behandlingId}' - godkjenner behandlingen, oppretter vedtak, evt. genererer meldekort og asynkront sender brev." }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.iverksett(behandlingId, saksbehandler, correlationId = call.correlationId())

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Beslutter godkjenner behandlingen",
            correlationId = call.correlationId(),
        )

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
