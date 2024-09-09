package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.institusjonsoppholdRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    auditService: AuditService,
) {
    hentInstitusjonsoppholdRoute(innloggetSaksbehandlerProvider, behandlingService, auditService)
}
