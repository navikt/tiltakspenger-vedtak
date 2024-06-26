package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.kvpRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    kvpVilkårService: KvpVilkårService,
    behandlingService: BehandlingService,
) {
    oppdaterKvpRoute(innloggetSaksbehandlerProvider, kvpVilkårService)
    hentKvpRoute(innloggetSaksbehandlerProvider, behandlingService)
}
