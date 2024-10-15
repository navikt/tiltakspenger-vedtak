package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

internal const val MELDEKORT_PATH = "/meldekort"

fun Route.meldekortRoutes(
    hentMeldekortService: HentMeldekortService,
    iverksettMeldekortService: IverksettMeldekortService,
    sendMeldekortTilBeslutterService: SendMeldekortTilBeslutterService,
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    auditService: AuditService,
    sakService: SakService,
    tokenService: TokenService,
) {
    hentMeldekortRoute(hentMeldekortService, sakService, auditService, tokenService)
    iverksettMeldekortRoute(iverksettMeldekortService, innloggetSaksbehandlerProvider, auditService)
    sendMeldekortTilBeslutterRoute(sendMeldekortTilBeslutterService, innloggetSaksbehandlerProvider, auditService)
}
