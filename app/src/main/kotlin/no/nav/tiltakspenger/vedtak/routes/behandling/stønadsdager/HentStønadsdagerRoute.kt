package no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.hentStønadsdagerRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    get("$BEHANDLING_PATH/{behandlingId}/stonadsdager") {
        SECURELOG.debug("Mottatt request på $BEHANDLING_PATH/{behandlingId}/stonadsdager")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.hentBehandling(behandlingId, saksbehandler).let {
            call.respond(
                status = HttpStatusCode.OK,
                message = it.stønadsdager.toDTO(),
            )
        }
    }
}
