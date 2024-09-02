package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
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
) {
    hentMeldekortRoute(hentMeldekortService, innloggetSaksbehandlerProvider)
    iverksettMeldekortRoute(iverksettMeldekortService, innloggetSaksbehandlerProvider)
    sendMeldekortTilBeslutterRoute(sendMeldekortTilBeslutterService, innloggetSaksbehandlerProvider)
}
