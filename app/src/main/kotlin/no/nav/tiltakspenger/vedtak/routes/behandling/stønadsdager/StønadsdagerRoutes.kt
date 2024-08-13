package no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.stønadsdagerRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    hentStønadsdagerRoute(innloggetSaksbehandlerProvider, behandlingService)
}
