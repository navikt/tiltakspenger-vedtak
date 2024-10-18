package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.withBehandlingId
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler

fun Route.hentIntroRoute(
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    val logger = KotlinLogging.logger {}
    get("$BEHANDLING_PATH/{behandlingId}/vilkar/introduksjonsprogrammet") {
        logger.debug("Mottatt get-request på '$BEHANDLING_PATH/{behandlingId}/vilkar/introduksjonsprogrammet' - henter vilkår om introduksjonsprogrammet")

        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withBehandlingId { behandlingId ->
                val correlationId = call.correlationId()
                behandlingService.hentBehandling(behandlingId, saksbehandler, correlationId = correlationId)
                    .let {
                        auditService.logMedBehandlingId(
                            behandlingId = behandlingId,
                            navIdent = saksbehandler.navIdent,
                            action = AuditLogEvent.Action.ACCESS,
                            contextMessage = "Henter vilkår om introduksjonsprogrammet",
                            correlationId = correlationId,
                        )
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = it.vilkårssett.introVilkår.toDTO(),
                        )
                    }
            }
        }
    }
}
