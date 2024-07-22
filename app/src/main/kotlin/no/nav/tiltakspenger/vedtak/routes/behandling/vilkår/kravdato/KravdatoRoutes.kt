package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravdato

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.kravdatoRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    hentKravdatoRoute(innloggetSaksbehandlerProvider, behandlingService)
}
