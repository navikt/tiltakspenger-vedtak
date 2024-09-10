package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.service.AuditLogEvent
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.hentAlderRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    auditService: AuditService,
) {
    get("$BEHANDLING_PATH/{behandlingId}/vilkar/alder") {
        SECURELOG.debug("Mottatt request på $BEHANDLING_PATH/{behandlingId}/vilkar/alder")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.hentBehandling(behandlingId, saksbehandler).let {
            auditService.logMedBehandlingId(
                behandlingId = behandlingId,
                navIdent = saksbehandler.navIdent,
                action = AuditLogEvent.Action.ACCESS,
                contextMessage = "Henter vilkår om alder",
                callId = call.callId,
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = it.vilkårssett.alderVilkår.toDTO(),
            )
        }
    }
}
