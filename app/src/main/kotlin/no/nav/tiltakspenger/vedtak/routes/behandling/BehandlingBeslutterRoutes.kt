package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.libs.auth.ktor.withSaksbehandler
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.withBehandlingId
import no.nav.tiltakspenger.vedtak.routes.withBody

data class BegrunnelseDTO(
    val begrunnelse: String,
)

fun Route.behandlingBeslutterRoutes(
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    val logger = KotlinLogging.logger {}
    post("$BEHANDLING_PATH/sendtilbake/{behandlingId}") {
        logger.debug("Mottatt post-request på '$BEHANDLING_PATH/sendtilbake/{behandlingId}' - sender behandling tilbake til saksbehandler")
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withBehandlingId { behandlingId ->
                call.withBody<BegrunnelseDTO> { body ->
                    val begrunnelse = body.begrunnelse
                    val correlationId = call.correlationId()
                    behandlingService.sendTilbakeTilSaksbehandler(
                        behandlingId = behandlingId,
                        beslutter = saksbehandler,
                        begrunnelse = begrunnelse,
                        correlationId = correlationId,
                    )
                    auditService.logMedBehandlingId(
                        behandlingId = behandlingId,
                        navIdent = saksbehandler.navIdent,
                        action = AuditLogEvent.Action.UPDATE,
                        contextMessage = "Beslutter underkjenner behandling",
                        correlationId = correlationId,
                    )
                    call.respond(status = HttpStatusCode.OK, message = "{}")
                }
            }
        }
    }

    post("$BEHANDLING_PATH/godkjenn/{behandlingId}") {
        logger.debug { "Mottatt post-request på '$BEHANDLING_PATH/godkjenn/{behandlingId}' - godkjenner behandlingen, oppretter vedtak, evt. genererer meldekort og asynkront sender brev." }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withBehandlingId { behandlingId ->
                val correlationId = call.correlationId()
                behandlingService.iverksett(behandlingId, saksbehandler, correlationId)

                auditService.logMedBehandlingId(
                    behandlingId = behandlingId,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.UPDATE,
                    contextMessage = "Beslutter godkjenner behandlingen",
                    correlationId = correlationId,
                )
                call.respond(message = "{}", status = HttpStatusCode.OK)
            }
        }
    }
}
