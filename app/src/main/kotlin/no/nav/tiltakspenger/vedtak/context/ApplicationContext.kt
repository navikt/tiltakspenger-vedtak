package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory

/**
 * Inneholder alle klienter, repoer og servicer.
 * Tanken er at man kan erstatte klienter og repoer med Fakes for testing.
 */
@Suppress("unused")
open class ApplicationContext(
    val sessionFactory: SessionFactory,
    private val gitHash: String,
) {
    open val personContext by lazy { PersonContext(sessionFactory) }
    open val dokumentContext by lazy { DokumentContext() }
    open val statistikkContext by lazy { StatistikkContext(sessionFactory) }
    open val søknadContext by lazy { SøknadContext(sessionFactory) }
    open val tilgangsstyringContext by lazy { TilgangsstyringContext(personContext.tokenProviderPdl::getToken) }
    open val tiltakContext by lazy { TiltakContext() }
    open val sakContext by lazy {
        SakContext(
            sessionFactory = sessionFactory,
            personGateway = personContext.personGateway,
            søknadService = søknadContext.søknadService,
            skjermingGateway = tilgangsstyringContext.skjermingGateway,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            tiltakGateway = tiltakContext.tiltakGateway,
            gitHash = gitHash,
        )
    }
    open val meldekortContext by lazy {
        MeldekortContext(
            sessionFactory = sessionFactory,
            sakService = sakContext.sakService,
            tilgangsstyringService = tilgangsstyringContext.tilgangsstyringService,
        )
    }
    open val førstegangsbehandlingContext by lazy {
        FørstegangsbehandlingContext(
            sessionFactory = sessionFactory,
            personopplysningRepo = personContext.personopplysningerRepo,
            meldekortRepo = meldekortContext.meldekortRepo,
            sakRepo = sakContext.sakRepo,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            gitHash = gitHash,
            journalførVedtaksbrevGateway = dokumentContext.journalførVedtaksbrevGateway,
            genererVedtaksbrevGateway = dokumentContext.genererVedtaksbrevGateway,
            dokdistGateway = dokumentContext.dokdistGateway,
        )
    }
    open val utbetalingContext by lazy {
        UtbetalingContext(
            sessionFactory = sessionFactory,
            rammevedtakRepo = førstegangsbehandlingContext.rammevedtakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            genererMeldekortPdfGateway = dokumentContext.genererMeldekortPdfGateway,
            journalførMeldekortGateway = dokumentContext.journalførMeldekortGateway,
        )
    }
}
