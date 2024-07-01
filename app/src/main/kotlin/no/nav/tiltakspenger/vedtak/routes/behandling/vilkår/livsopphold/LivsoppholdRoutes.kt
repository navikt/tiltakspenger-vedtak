package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.livsoppholdRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    livsoppholdVilkårService: LivsoppholdVilkårService,
    behandlingService: BehandlingService,
) {
    oppdaterLivsoppholdRoute(innloggetSaksbehandlerProvider, livsoppholdVilkårService)
    // TODO hentLivsoppholdRoute(innloggetSaksbehandlerProvider, behandlingService)
}
