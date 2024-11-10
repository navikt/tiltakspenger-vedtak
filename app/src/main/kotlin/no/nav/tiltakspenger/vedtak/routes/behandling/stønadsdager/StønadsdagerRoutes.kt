package no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService

fun Route.stønadsdagerRoutes(
    behandlingService: BehandlingService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    hentStønadsdagerRoute(tokenService, behandlingService, auditService)
}
