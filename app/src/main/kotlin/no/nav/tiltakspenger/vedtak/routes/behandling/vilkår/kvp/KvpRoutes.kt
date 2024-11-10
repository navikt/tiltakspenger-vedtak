package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService

fun Route.kvpRoutes(
    kvpVilkårService: KvpVilkårService,
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    oppdaterKvpRoute(kvpVilkårService, auditService, tokenService)
    hentKvpRoute(behandlingService, auditService, tokenService)
}
