package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService

fun Route.livsoppholdRoutes(
    livsoppholdVilkårService: LivsoppholdVilkårService,
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    oppdaterLivsoppholdRoute(livsoppholdVilkårService, auditService, tokenService)
    hentLivsoppholdRoute(behandlingService, auditService, tokenService)
}
