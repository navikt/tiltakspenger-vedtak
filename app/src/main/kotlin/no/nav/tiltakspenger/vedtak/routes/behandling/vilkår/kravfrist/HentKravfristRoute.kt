package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist

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

fun Route.hentKravfristRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    get("$BEHANDLING_PATH/{behandlingId}/vilkar/kravfrist") {
        SECURELOG.debug("Mottatt request på $BEHANDLING_PATH/{behandlingId}/vilkar/kravfrist")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.hentBehandling(behandlingId, saksbehandler).let {
            call.respond(
                status = HttpStatusCode.OK,
                message = it.vilkårssett.kravfristVilkår.toDTO(),
            )
        }
    }
}
