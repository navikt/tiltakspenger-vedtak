package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService

fun Route.livsoppholdRoutes(
    livsoppholdVilkårService: LivsoppholdVilkårService,
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    oppdaterLivsoppholdRoute(livsoppholdVilkårService, auditService, tokenService)
    hentLivsoppholdRoute(behandlingService, auditService, tokenService)
}
