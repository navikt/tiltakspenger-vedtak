package no.nav.tiltakspenger.common

import no.nav.tiltakspenger.fakes.clients.DokdistFakeGateway
import no.nav.tiltakspenger.fakes.clients.EntraIdSystemtokenFakeClient
import no.nav.tiltakspenger.fakes.clients.GenererFakeMeldekortPdfGateway
import no.nav.tiltakspenger.fakes.clients.GenererFakeVedtaksbrevGateway
import no.nav.tiltakspenger.fakes.clients.JournalførFakeMeldekortGateway
import no.nav.tiltakspenger.fakes.clients.JournalførFakeVedtaksbrevGateway
import no.nav.tiltakspenger.fakes.clients.JwkFakeProvider
import no.nav.tiltakspenger.fakes.clients.PersonFakeGateway
import no.nav.tiltakspenger.fakes.clients.TilgangsstyringFakeGateway
import no.nav.tiltakspenger.fakes.clients.TiltakFakeGateway
import no.nav.tiltakspenger.fakes.clients.UtbetalingFakeGateway
import no.nav.tiltakspenger.fakes.repos.BehandlingFakeRepo
import no.nav.tiltakspenger.fakes.repos.MeldekortFakeRepo
import no.nav.tiltakspenger.fakes.repos.PersonFakeRepo
import no.nav.tiltakspenger.fakes.repos.RammevedtakFakeRepo
import no.nav.tiltakspenger.fakes.repos.SakFakeRepo
import no.nav.tiltakspenger.fakes.repos.SaksoversiktFakeRepo
import no.nav.tiltakspenger.fakes.repos.StatistikkSakFakeRepo
import no.nav.tiltakspenger.fakes.repos.StatistikkStønadFakeRepo
import no.nav.tiltakspenger.fakes.repos.SøknadFakeRepo
import no.nav.tiltakspenger.fakes.repos.UtbetalingsvedtakFakeRepo
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.TestSessionFactory
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.auth2.MicrosoftEntraIdTokenService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.context.ApplicationContext
import no.nav.tiltakspenger.vedtak.context.DokumentContext
import no.nav.tiltakspenger.vedtak.context.FørstegangsbehandlingContext
import no.nav.tiltakspenger.vedtak.context.MeldekortContext
import no.nav.tiltakspenger.vedtak.context.PersonContext
import no.nav.tiltakspenger.vedtak.context.SakContext
import no.nav.tiltakspenger.vedtak.context.StatistikkContext
import no.nav.tiltakspenger.vedtak.context.SøknadContext
import no.nav.tiltakspenger.vedtak.context.TiltakContext
import no.nav.tiltakspenger.vedtak.context.UtbetalingContext

/**
 * Oppretter en tom ApplicationContext for bruk i tester.
 * Dette vil tilsvare en tom intern database og tomme fakes for eksterne tjenester.
 * Bruk service-funksjoner og hjelpemetoder for å legge til data.
 */
class TestApplicationContext : ApplicationContext(TestSessionFactory(), "fake-git-hash") {
    val journalpostIdGenerator = JournalpostIdGenerator()
    val distribusjonIdGenerator = DistribusjonIdGenerator()

    private val rammevedtakFakeRepo = RammevedtakFakeRepo()
    private val statistikkStønadFakeRepo = StatistikkStønadFakeRepo()
    private val statistikkSakFakeRepo = StatistikkSakFakeRepo()
    private val utbetalingGatewayFake = UtbetalingFakeGateway()
    private val meldekortFakeRepo = MeldekortFakeRepo()
    private val utbetalingsvedtakFakeRepo = UtbetalingsvedtakFakeRepo(rammevedtakFakeRepo, meldekortFakeRepo)
    private val søknadFakeRepo = SøknadFakeRepo()
    private val tiltakGatewayFake = TiltakFakeGateway()
    private val behandlingFakeRepo = BehandlingFakeRepo()
    private val personGatewayFake = PersonFakeGateway()
    private val tilgangsstyringFakeGateway = TilgangsstyringFakeGateway()
    private val genererFakeMeldekortPdfGateway = GenererFakeMeldekortPdfGateway()
    private val genererFakeVedtaksbrevGateway = GenererFakeVedtaksbrevGateway()
    private val journalførFakeMeldekortGateway = JournalførFakeMeldekortGateway(journalpostIdGenerator)
    private val journalførFakeVedtaksbrevGateway = JournalførFakeVedtaksbrevGateway(journalpostIdGenerator)
    private val dokdistFakeGateway = DokdistFakeGateway(distribusjonIdGenerator)

    val jwtGenerator = JwtGenerator()

    override val tokenService: TokenService = MicrosoftEntraIdTokenService(
        url = "unused",
        issuer = "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0",
        clientId = "c7adbfbb-1b1e-41f6-9b7a-af9627c04998",
        autoriserteBrukerroller = Rolle.entries.map { AdRolle(it, "ROLE_${it.name}") },
        acceptIssuedAtLeeway = 0,
        acceptNotBeforeLeeway = 0,
        provider = JwkFakeProvider(jwtGenerator.jwk),
    )

    fun leggTilPerson(
        fnr: Fnr,
        personopplysningerForBruker: PersonopplysningerSøker,
        tiltak: Tiltak,
    ) {
        personGatewayFake.leggTilPersonopplysning(fnr = fnr, personopplysninger = personopplysningerForBruker)
        tilgangsstyringFakeGateway.lagre(
            fnr = fnr,
            adressebeskyttelseGradering = listOf(AdressebeskyttelseGradering.UGRADERT),
        )
        tiltakGatewayFake.lagre(fnr = fnr, tiltak = tiltak)
    }

    private val saksoversiktFakeRepo =
        SaksoversiktFakeRepo(
            søknadFakeRepo = søknadFakeRepo,
            behandlingFakeRepo = behandlingFakeRepo,
        )
    private val sakFakeRepo =
        SakFakeRepo(
            behandlingRepo = behandlingFakeRepo,
            rammevedtakRepo = rammevedtakFakeRepo,
            meldekortRepo = meldekortFakeRepo,
            utbetalingsvedtakRepo = utbetalingsvedtakFakeRepo,
        )

    private val personFakeRepo = PersonFakeRepo(sakFakeRepo, søknadFakeRepo, meldekortFakeRepo)

    override val entraIdSystemtokenClient = EntraIdSystemtokenFakeClient()

    override val personContext =
        object : PersonContext(sessionFactory, entraIdSystemtokenClient) {
            override val personGateway = personGatewayFake
            override val personRepo = personFakeRepo
        }
    override val dokumentContext by lazy {
        object : DokumentContext(entraIdSystemtokenClient) {
            override val journalførMeldekortGateway = journalførFakeMeldekortGateway
            override val journalførVedtaksbrevGateway = journalførFakeVedtaksbrevGateway
            override val genererMeldekortPdfGateway = genererFakeMeldekortPdfGateway
            override val genererVedtaksbrevGateway = genererFakeVedtaksbrevGateway
        }
    }

    override val statistikkContext by lazy {
        object : StatistikkContext(sessionFactory) {
            override val statistikkStønadRepo = statistikkStønadFakeRepo
            override val statistikkSakRepo = statistikkSakFakeRepo
        }
    }

    override val søknadContext by lazy {
        object : SøknadContext(sessionFactory) {
            override val søknadRepo = søknadFakeRepo
        }
    }

    override val tiltakContext by lazy {
        object : TiltakContext(entraIdSystemtokenClient) {
            override val tiltakGateway = tiltakGatewayFake
        }
    }
    override val sakContext by lazy {
        object : SakContext(
            sessionFactory = sessionFactory,
            personService = personContext.personService,
            søknadService = søknadContext.søknadService,
            statistikkSakRepo = statistikkSakFakeRepo,
            tiltakGateway = tiltakGatewayFake,
            tilgangsstyringService = tilgangsstyringFakeGateway,
            gitHash = "fake-git-hash",
        ) {
            override val sakRepo = sakFakeRepo
            override val saksoversiktRepo = saksoversiktFakeRepo
        }
    }

    override val meldekortContext by lazy {
        object :
            MeldekortContext(
                sessionFactory = sessionFactory,
                sakService = sakContext.sakService,
                tilgangsstyringService = tilgangsstyringFakeGateway,
                utbetalingsvedtakRepo = utbetalingsvedtakFakeRepo,
                statistikkStønadRepo = statistikkStønadFakeRepo,
                personService = personContext.personService,

            ) {
            override val meldekortRepo = meldekortFakeRepo
        }
    }

    override val førstegangsbehandlingContext by lazy {
        object : FørstegangsbehandlingContext(
            sessionFactory = sessionFactory,
            meldekortRepo = meldekortFakeRepo,
            sakRepo = sakFakeRepo,
            statistikkSakRepo = statistikkSakFakeRepo,
            statistikkStønadRepo = statistikkStønadFakeRepo,
            gitHash = "fake-git-hash",
            journalførVedtaksbrevGateway = journalførFakeVedtaksbrevGateway,
            genererVedtaksbrevGateway = genererFakeVedtaksbrevGateway,
            personService = personContext.personService,
            tilgangsstyringService = tilgangsstyringFakeGateway,
            dokdistGateway = dokdistFakeGateway,
            navIdentClient = personContext.navIdentClient,
        ) {
            override val rammevedtakRepo = rammevedtakFakeRepo
            override val behandlingRepo = behandlingFakeRepo
        }
    }

    override val utbetalingContext by lazy {
        object : UtbetalingContext(
            sessionFactory = sessionFactory,
            genererMeldekortPdfGateway = genererFakeMeldekortPdfGateway,
            journalførMeldekortGateway = journalførFakeMeldekortGateway,
            personService = personContext.personService,
            entraIdSystemtokenClient = entraIdSystemtokenClient,
        ) {
            override val utbetalingGateway = utbetalingGatewayFake
            override val utbetalingsvedtakRepo = utbetalingsvedtakFakeRepo
        }
    }
}
