package no.nav.tiltakspenger.vedtak.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService

private val LOG = KotlinLogging.logger {}

internal const val UTBETALING_PATH = "/utbetaling"

internal fun Route.utbetalingRoutes(hentUtbetalingsvedtakService: HentUtbetalingsvedtakService) {
    get("$UTBETALING_PATH/hentAlleForBehandling/{behandlingId}") {
        val id = call.parameters["behandlingId"]
        LOG.info { "hent alle utbetalingsvedtak for behandling $id" }

        checkNotNull(id) { "Mangler BehandlingId" }
        val behandlingId = BehandlingId.fromString(id)
        val vedtak = hentUtbetalingsvedtakService.hentForBehandlingId(behandlingId).filterNot { it.utbetalingsperiode.isEmpty() }

        call.respond(status = HttpStatusCode.OK, mapAlleVedtak(vedtak))
    }

    get("$UTBETALING_PATH/hentVedtak/{vedtakId}") {
        val id = call.parameters["vedtakId"]
        LOG.info { "hent vedtak for id $id" }

        checkNotNull(id) { "Mangler VedtakId" }
        val vedtakId = VedtakId.fromString(id)

        val vedtak = hentUtbetalingsvedtakService.hentForVedtakId(vedtakId)
        checkNotNull(vedtak) { "Fant ikke vedtak" }

        call.respond(status = HttpStatusCode.OK, mapVedtak(vedtak))
    }
}
