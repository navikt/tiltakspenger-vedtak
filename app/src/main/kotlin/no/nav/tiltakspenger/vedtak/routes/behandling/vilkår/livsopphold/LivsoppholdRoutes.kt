package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.kvpRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    kvpVilkårService: KvpVilkårService,
    behandlingService: BehandlingService,
) {
    oppdaterLivsoppholdRoute(innloggetSaksbehandlerProvider, kvpVilkårService)
    hentLivsoppholdRoute(innloggetSaksbehandlerProvider, behandlingService)
}
