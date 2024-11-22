package no.nav.tiltakspenger

import no.nav.tiltakspenger.common.DistribusjonIdGenerator
import no.nav.tiltakspenger.common.JournalpostIdGenerator
import no.nav.tiltakspenger.fakes.clients.DokdistFakeGateway
import no.nav.tiltakspenger.fakes.clients.FellesFakeAdressebeskyttelseKlient
import no.nav.tiltakspenger.fakes.clients.FellesFakeSkjermingsklient
import no.nav.tiltakspenger.fakes.clients.GenererFakeUtbetalingsvedtakGateway
import no.nav.tiltakspenger.fakes.clients.GenererFakeVedtaksbrevGateway
import no.nav.tiltakspenger.fakes.clients.JournalførFakeMeldekortGateway
import no.nav.tiltakspenger.fakes.clients.JournalførFakeVedtaksbrevGateway
import no.nav.tiltakspenger.fakes.clients.PersonFakeGateway
import no.nav.tiltakspenger.fakes.clients.PoaoTilgangskontrollFake
import no.nav.tiltakspenger.fakes.clients.TiltakFakeGateway
import no.nav.tiltakspenger.fakes.clients.UtbetalingFakeGateway
import no.nav.tiltakspenger.libs.auth.test.core.EntraIdSystemtokenFakeClient
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.vedtak.Profile
import no.nav.tiltakspenger.vedtak.context.ApplicationContext
import no.nav.tiltakspenger.vedtak.context.DokumentContext
import no.nav.tiltakspenger.vedtak.context.FørstegangsbehandlingContext
import no.nav.tiltakspenger.vedtak.context.MeldekortContext
import no.nav.tiltakspenger.vedtak.context.PersonContext
import no.nav.tiltakspenger.vedtak.context.SakContext
import no.nav.tiltakspenger.vedtak.context.TiltakContext
import no.nav.tiltakspenger.vedtak.context.UtbetalingContext

/**
 * Oppretter en tom ApplicationContext for bruk i tester.
 * Dette vil tilsvare en tom intern database og tomme fakes for eksterne tjenester.
 * Bruk service-funksjoner og hjelpemetoder for å legge til data.
 */
class LocalApplicationContext : ApplicationContext(gitHash = "fake-git-hash") {

    val journalpostIdGenerator = JournalpostIdGenerator()
    val distribusjonIdGenerator = DistribusjonIdGenerator()

    private val utbetalingGatewayFake = UtbetalingFakeGateway()
    private val tiltakGatewayFake = TiltakFakeGateway()
    private val personGatewayFake = PersonFakeGateway()
    private val genererFakeUtbetalingsvedtakGateway = GenererFakeUtbetalingsvedtakGateway()
    private val genererFakeVedtaksbrevGateway = GenererFakeVedtaksbrevGateway()
    private val journalførFakeMeldekortGateway = JournalførFakeMeldekortGateway(journalpostIdGenerator)
    private val journalførFakeVedtaksbrevGateway = JournalførFakeVedtaksbrevGateway(journalpostIdGenerator)
    private val dokdistFakeGateway = DokdistFakeGateway(distribusjonIdGenerator)
    private val fellesFakeAdressebeskyttelseKlient = FellesFakeAdressebeskyttelseKlient()
    private val fellesFakeSkjermingsklient = FellesFakeSkjermingsklient()
    private val poaoTilgangskontrollFake = PoaoTilgangskontrollFake()

    init {
        // Må henge sammen med V1004__sak.sql og V1011__soknad.sql (lokal migration)
        listOf(
            "29927899076",
            "12828098533",
            "24496837246",
            "04488337357",
            "15850598247",
            "24918297193",
            "04880298335",
            "25927798800",
            "22888699787",
            "14838098925",
            "21838099944",
            "31528732014",
            "16498425414",
        ).forEach {
            val fnr = Fnr.fromString(it)
            leggTilPerson(
                fnr = fnr,
                personopplysningerForBruker = ObjectMother.personopplysningKjedeligFyr(fnr = fnr),
                tiltak = ObjectMother.tiltak(),
            )
        }
    }

    fun leggTilPerson(
        fnr: Fnr,
        personopplysningerForBruker: PersonopplysningerSøker,
        tiltak: Tiltak,
    ) {
        fellesFakeSkjermingsklient.leggTil(fnr = fnr, skjermet = false)
        fellesFakeAdressebeskyttelseKlient.leggTil(fnr = fnr, gradering = listOf(AdressebeskyttelseGradering.UGRADERT))
        personGatewayFake.leggTilPersonopplysning(fnr = fnr, personopplysninger = personopplysningerForBruker)
        tiltakGatewayFake.lagre(fnr = fnr, tiltak = tiltak)
        poaoTilgangskontrollFake.leggTil(fnr = fnr, skjermet = false)
    }

    override val entraIdSystemtokenClient = EntraIdSystemtokenFakeClient()

    override val personContext =
        object : PersonContext(sessionFactory, entraIdSystemtokenClient) {
            override val personGateway = personGatewayFake
            override val tilgangsstyringService = TilgangsstyringServiceImpl(
                fellesPersonTilgangsstyringsklient = fellesFakeAdressebeskyttelseKlient,
                skjermingClient = fellesFakeSkjermingsklient,
            )
            override val poaoTilgangGateway = poaoTilgangskontrollFake
        }
    override val dokumentContext by lazy {
        object : DokumentContext(entraIdSystemtokenClient) {
            override val journalførMeldekortGateway = journalførFakeMeldekortGateway
            override val journalførVedtaksbrevGateway = journalførFakeVedtaksbrevGateway
            override val genererUtbetalingsvedtakGateway = genererFakeUtbetalingsvedtakGateway
            override val genererVedtaksbrevGateway = genererFakeVedtaksbrevGateway
        }
    }

    override val tiltakContext by lazy {
        object : TiltakContext(entraIdSystemtokenClient) {
            override val tiltakGateway = tiltakGatewayFake
        }
    }
    override val profile by lazy { Profile.LOCAL }
    override val sakContext by lazy {
        object : SakContext(
            sessionFactory = sessionFactory,
            personService = personContext.personService,
            søknadService = søknadContext.søknadService,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            tiltakGateway = tiltakGatewayFake,
            tilgangsstyringService = personContext.tilgangsstyringService,
            poaoTilgangGateway = personContext.poaoTilgangGateway,
            gitHash = gitHash,
            profile = profile,
        ) {}
    }
    override val meldekortContext by lazy {
        object : MeldekortContext(
            sessionFactory = sessionFactory,
            sakService = sakContext.sakService,
            tilgangsstyringService = personContext.tilgangsstyringService,
            utbetalingsvedtakRepo = utbetalingContext.utbetalingsvedtakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            personService = personContext.personService,
        ) {}
    }
    override val førstegangsbehandlingContext by lazy {
        object : FørstegangsbehandlingContext(
            sessionFactory = sessionFactory,
            meldekortRepo = meldekortContext.meldekortRepo,
            sakRepo = sakContext.sakRepo,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            gitHash = "fake-git-hash",
            journalførVedtaksbrevGateway = journalførFakeVedtaksbrevGateway,
            genererVedtaksbrevGateway = genererFakeVedtaksbrevGateway,
            personService = personContext.personService,
            tilgangsstyringService = personContext.tilgangsstyringService,
            dokdistGateway = dokdistFakeGateway,
            navIdentClient = personContext.navIdentClient,
        ) {}
    }
    override val utbetalingContext by lazy {
        object : UtbetalingContext(
            sessionFactory = sessionFactory,
            genererUtbetalingsvedtakGateway = genererFakeUtbetalingsvedtakGateway,
            journalførMeldekortGateway = journalførFakeMeldekortGateway,
            entraIdSystemtokenClient = entraIdSystemtokenClient,
            sakRepo = sakContext.sakRepo,
            navIdentClient = personContext.navIdentClient,
        ) {
            override val utbetalingGateway = utbetalingGatewayFake
        }
    }
}
