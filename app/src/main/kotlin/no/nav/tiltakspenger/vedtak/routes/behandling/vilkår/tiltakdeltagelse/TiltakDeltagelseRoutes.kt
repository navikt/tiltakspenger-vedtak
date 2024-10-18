package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService

fun Route.tiltakDeltagelseRoutes(
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    hentTiltakDeltagelseRoute(behandlingService, auditService, tokenService)
}
