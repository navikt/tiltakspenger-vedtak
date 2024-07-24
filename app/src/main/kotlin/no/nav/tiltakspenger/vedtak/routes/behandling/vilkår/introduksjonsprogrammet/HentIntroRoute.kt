package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.hentIntroRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,

    behandlingService: BehandlingService,
) {
    get("$behandlingPath/{behandlingId}/vilkar/introduksjonsprogrammet") {
        SECURELOG.debug("Mottatt request på $behandlingPath/{behandlingId}/vilkar/introduksjonsprogrammet")

        innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.hentBehandling(behandlingId).let {
            call.respond(
                status = HttpStatusCode.OK,
                message = it.vilkårssett.introVilkår.toDTO(),
            )
        }
    }
}
