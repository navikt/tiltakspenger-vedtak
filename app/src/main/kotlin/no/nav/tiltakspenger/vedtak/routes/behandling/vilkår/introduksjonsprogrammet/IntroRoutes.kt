package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService

fun Route.introRoutes(
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    hentIntroRoute(behandlingService, auditService, tokenService)
}
