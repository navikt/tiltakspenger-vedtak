package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService

fun Route.kvpRoutes(
    kvpVilkårService: KvpVilkårService,
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    oppdaterKvpRoute(kvpVilkårService, auditService, tokenService)
    hentKvpRoute(behandlingService, auditService, tokenService)
}
