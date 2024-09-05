package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory

/**
 * Inneholder alle klienter, repoer, jobber og servicer.
 */
@Suppress("unused")
internal open class ApplicationContext(
    val dokumentContext: DokumentContext,
    val førstegangsbehandlingContext: FørstegangsbehandlingContext,
    val meldekortContext: MeldekortContext,
    val personContext: PersonContext,
    val sakContext: SakContext,
    val statistikkContext: StatistikkContext,
    val søknadContext: SøknadContext,
    val tilgangsstyringContext: TilgangsstyringContext,
    val tiltakContext: TiltakContext,
    val utbetalingContext: UtbetalingContext,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
        ): ApplicationContext {
            val søknadContext = SøknadContext.create(
                sessionFactory = sessionFactory,
            )
            val dokumentContext = DokumentContext.create()
            val personContext = PersonContext.create(
                sessionFactory = sessionFactory,
            )
            val statistikkContext = StatistikkContext.create(
                sessionFactory = sessionFactory,
            )
            val tilgangsstyringContext = TilgangsstyringContext.create(
                tokenProviderPdl = personContext.tokenProviderPdl,
            )
            val tiltakContext = TiltakContext.create()
            val sakContext = SakContext.create(
                sessionFactory = sessionFactory,
                personGateway = personContext.personGateway,
                søknadService = søknadContext.søknadService,
                skjermingGateway = tilgangsstyringContext.skjermingGateway,
                statistikkSakRepo = statistikkContext.statistikkSakRepo,
                tiltakGateway = tiltakContext.tiltakGateway,
            )
            val meldekortContext = MeldekortContext.create(
                sessionFactory = sessionFactory,
                sakService = sakContext.sakService,
                tilgangsstyringService = tilgangsstyringContext.tilgangsstyringService,
                dokumentGateway = dokumentContext.dokumentGateway,
            )
            val førstegangsbehandlingContext = FørstegangsbehandlingContext.create(
                sessionFactory = sessionFactory,
                personopplysningRepo = personContext.personopplysningRepo,
                meldekortRepo = meldekortContext.meldekortRepo,
                sakRepo = sakContext.sakRepo,
                statistikkSakRepo = statistikkContext.statistikkSakRepo,
                statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            )

            val utbetalingContext = UtbetalingContext.create(
                sessionFactory = sessionFactory,
                rammevedtakRepo = førstegangsbehandlingContext.rammevedtakRepo,
                statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
                dokumentGateway = dokumentContext.dokumentGateway,
            )
            return ApplicationContext(
                dokumentContext = dokumentContext,
                personContext = personContext,
                statistikkContext = statistikkContext,
                søknadContext = søknadContext,
                førstegangsbehandlingContext = førstegangsbehandlingContext,
                meldekortContext = meldekortContext,
                sakContext = sakContext,
                tilgangsstyringContext = tilgangsstyringContext,
                tiltakContext = tiltakContext,
                utbetalingContext = utbetalingContext,
            )
        }
    }
}
