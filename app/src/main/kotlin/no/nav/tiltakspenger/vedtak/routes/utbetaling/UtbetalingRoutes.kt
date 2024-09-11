package no.nav.tiltakspenger.vedtak.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val UTBETALING_PATH = "/utbetaling"

internal fun Route.utbetalingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    hentUtbetalingsvedtakService: HentUtbetalingsvedtakService,
    auditService: AuditService,
) {
    get("$UTBETALING_PATH/hentAlleForBehandling/{behandlingId}") {
        val id = call.parameters["behandlingId"]
        LOG.info { "hent alle utbetalingsvedtak for behandling $id" }
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        checkNotNull(id) { "Mangler BehandlingId" }
        val behandlingId = BehandlingId.fromString(id)
        val vedtak = hentUtbetalingsvedtakService.hentForBehandlingId(behandlingId).filterNot { it.utbetalingsperiode.isEmpty() }

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter alle utbetalinger for en behandling",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, message = {})
    }

    get("$UTBETALING_PATH/hentVedtak/{vedtakId}") {
        val id = call.parameters["vedtakId"]
        LOG.info { "hent vedtak for id $id" }
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        checkNotNull(id) { "Mangler VedtakId" }
        val vedtakId = VedtakId.fromString(id)

        val vedtak = hentUtbetalingsvedtakService.hentForVedtakId(vedtakId)
        checkNotNull(vedtak) { "Fant ikke vedtak" }

        auditService.logMedVedtakId(
            vedtakId = vedtakId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter vedtak for behandlingen",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, message = {})
    }
}
