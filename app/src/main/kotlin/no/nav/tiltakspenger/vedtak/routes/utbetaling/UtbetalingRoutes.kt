package no.nav.tiltakspenger.vedtak.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.utbetaling.UtbetalingService

private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetalingRoutes(
    behandlingService: BehandlingService,
    utbetalingService: UtbetalingService,
) {
    post("$utbetalingPath/sendtilutbetaling/{behandlingId}") {
        SECURELOG.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        val behandling = behandlingService.hentBehandling(behandlingId)
            ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        utbetalingService.sendBehandlingTilUtbetaling(behandling)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
