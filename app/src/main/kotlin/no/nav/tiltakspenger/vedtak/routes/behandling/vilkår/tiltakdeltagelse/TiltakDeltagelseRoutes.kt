package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.tiltakDeltagelseRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    hentTiltakDeltagelseRoute(innloggetSaksbehandlerProvider, behandlingService)
}
