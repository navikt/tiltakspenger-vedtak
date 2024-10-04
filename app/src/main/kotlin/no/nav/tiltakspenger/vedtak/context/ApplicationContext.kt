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
    open val tiltakContext by lazy { TiltakContext() }
    open val sakContext by lazy {
        SakContext(
            sessionFactory = sessionFactory,
            søknadService = søknadContext.søknadService,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            tilgangsstyringService = personContext.tilgangsstyringService,
            tiltakGateway = tiltakContext.tiltakGateway,
            personService = personContext.personService,
            gitHash = gitHash,
        )
    }
    open val utbetalingContext by lazy {
        UtbetalingContext(
            sessionFactory = sessionFactory,
            genererMeldekortPdfGateway = dokumentContext.genererMeldekortPdfGateway,
            journalførMeldekortGateway = dokumentContext.journalførMeldekortGateway,
        )
    }
    open val meldekortContext by lazy {
        MeldekortContext(
            sessionFactory = sessionFactory,
            sakService = sakContext.sakService,
            tilgangsstyringService = personContext.tilgangsstyringService,
            utbetalingsvedtakRepo = utbetalingContext.utbetalingsvedtakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
        )
    }
    open val førstegangsbehandlingContext by lazy {
        FørstegangsbehandlingContext(
            sessionFactory = sessionFactory,
            meldekortRepo = meldekortContext.meldekortRepo,
            sakRepo = sakContext.sakRepo,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            gitHash = gitHash,
            journalførVedtaksbrevGateway = dokumentContext.journalførVedtaksbrevGateway,
            genererVedtaksbrevGateway = dokumentContext.genererVedtaksbrevGateway,
            personService = personContext.personService,
            tilgangsstyringService = personContext.tilgangsstyringService,
            dokdistGateway = dokumentContext.dokdistGateway,
        )
    }
}
