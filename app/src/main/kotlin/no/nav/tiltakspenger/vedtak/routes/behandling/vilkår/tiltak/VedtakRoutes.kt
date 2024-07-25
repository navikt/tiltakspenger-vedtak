package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltak

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.tiltakRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    hentTiltakRoute(innloggetSaksbehandlerProvider, behandlingService)
}
