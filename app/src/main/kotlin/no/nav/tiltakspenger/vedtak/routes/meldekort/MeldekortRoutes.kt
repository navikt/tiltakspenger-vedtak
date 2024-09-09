package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.server.routing.Route
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val MELDEKORT_PATH = "/meldekort"

fun Route.meldekortRoutes(
    hentMeldekortService: HentMeldekortService,
    iverksettMeldekortService: IverksettMeldekortService,
    sendMeldekortTilBeslutterService: SendMeldekortTilBeslutterService,
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    auditService: AuditService,
) {
    hentMeldekortRoute(hentMeldekortService, innloggetSaksbehandlerProvider, auditService)
    iverksettMeldekortRoute(iverksettMeldekortService, innloggetSaksbehandlerProvider, auditService)
    sendMeldekortTilBeslutterRoute(sendMeldekortTilBeslutterService, innloggetSaksbehandlerProvider, auditService)
}
