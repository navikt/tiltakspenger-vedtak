package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.tiltaksdeltagelse.TiltaksdeltagelseVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService

fun Route.tiltakDeltagelseRoutes(
    behandlingService: BehandlingService,
    tiltaksdeltagelseVilkårService: TiltaksdeltagelseVilkårService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    hentTiltakDeltagelseRoute(behandlingService, auditService, tokenService)
    leggTilTiltaksdeltagelseRoute(tiltaksdeltagelseVilkårService, auditService, tokenService)
}
