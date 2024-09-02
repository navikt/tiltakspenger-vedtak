package no.nav.tiltakspenger.vedtak

import arrow.core.Either
import arrow.core.right
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.libs.jobber.LeaderPodLookup
import no.nav.tiltakspenger.libs.jobber.LeaderPodLookupClient
import no.nav.tiltakspenger.libs.jobber.LeaderPodLookupFeil
import no.nav.tiltakspenger.libs.jobber.RunCheckFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.SessionCounter
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.meldekort.service.JournalførMeldekortService
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.saksbehandling.service.SøknadServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl
import no.nav.tiltakspenger.saksbehandling.service.vedtak.RammevedtakServiceImpl
import no.nav.tiltakspenger.utbetaling.client.iverksett.UtbetalingHttpClient
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.OpprettUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.SendUtbetalingerService
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.dokument.DokumentClient
import no.nav.tiltakspenger.vedtak.clients.person.PersonHttpklient
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingClientImpl
import no.nav.tiltakspenger.vedtak.clients.skjerming.SkjermingGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakClientImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl
import no.nav.tiltakspenger.vedtak.db.DataSourceSetup
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.jobber.TaskExecutor
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.benk.SaksoversiktPostgresRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnMedIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerBarnUtenIdentRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.statistikk.sak.StatistikkSakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.statistikk.stønad.StatistikkStønadRepoImpl
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadTiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.VedleggDAO
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakRepoImpl
import no.nav.tiltakspenger.vedtak.repository.vedtak.RammevedtakRepoImpl
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider

val log = KotlinLogging.logger {}
val securelog = KotlinLogging.logger("tjenestekall")

internal class ApplicationBuilder(
    @Suppress("UNUSED_PARAMETER") config: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val rapidConfig =
        if (Configuration.applicationProfile() == Profile.LOCAL) {
            RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers, LokalKafkaConfig())
        } else {
            RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
        }

    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .Builder(rapidConfig)
            .withKtorModule {
                vedtakApi(
                    config = Configuration.TokenVerificationConfig(),
                    innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                    sakService = sakService,
                    behandlingService = behandlingService,
                    kvpVilkårService = kvpVilkårService,
                    livsoppholdVilkårService = livsoppholdVilkårService,
                    søknadService = søknadService,
                    hentUtbetalingsvedtakService = hentUtbetalingsvedtakService,
                    hentMeldekortService = hentMeldekortService,
                    iverksettMeldekortService = iverksettMeldekortService,
                    sendMeldekortTilBeslutterService = sendMeldekortTilBeslutterService,
                )
            }.build()

    private val tokenProviderPdl: AzureTokenProvider =
        AzureTokenProvider(config = Configuration.ouathConfigPdl())
    private val tokenProviderSkjerming: AzureTokenProvider =
        AzureTokenProvider(config = Configuration.oauthConfigSkjerming())
    private val tokenProviderTiltak: AzureTokenProvider =
        AzureTokenProvider(config = Configuration.oauthConfigTiltak())
    private val tokenProviderUtbetaling = AzureTokenProvider(config = Configuration.oauthConfigUtbetaling())
    private val tokenProviderDokument = AzureTokenProvider(config = Configuration.oauthConfigDokument())

    private val dataSource = DataSourceSetup.createDatasource()
    private val sessionCounter = SessionCounter(log)
    private val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)

    private val skjermingClient = SkjermingClientImpl(getToken = tokenProviderSkjerming::getToken)
    private val tiltakClient = TiltakClientImpl(getToken = tokenProviderTiltak::getToken)
    private val skjermingGateway = SkjermingGatewayImpl(skjermingClient)
    private val tiltakGateway = TiltakGatewayImpl(tiltakClient)
    private val brevPublisherGateway = BrevPublisherGatewayImpl(rapidsConnection)

    private val personGateway =
        PersonHttpklient(endepunkt = Configuration.pdlClientConfig().baseUrl, azureTokenProvider = tokenProviderPdl)

    private val utbetalingsklient: UtbetalingGateway =
        UtbetalingHttpClient(
            endepunkt = Configuration.utbetalingClientConfig().baseUrl,
            getToken = tokenProviderUtbetaling::getToken,
        )

    private val dokumentGateway = DokumentClient(
        baseUrl = Configuration.dokumentClientConfig().baseUrl,
        getToken = tokenProviderDokument::getToken,
    )
    private val utfyltMeldekortRepo = MeldekortRepoImpl(sessionFactory)
    private val utbetalingsvedtakRepo =
        UtbetalingsvedtakRepoImpl(
            sessionFactory = sessionFactory,
            meldekortRepo = utfyltMeldekortRepo,
        )
    private val hentUtbetalingsvedtakService =
        HentUtbetalingsvedtakService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
        )

    private val sendeMeldekortTilBeslutterService =
        SendMeldekortTilBeslutterService(
            meldekortRepo = utfyltMeldekortRepo,
        )
    private val sendUtbetalingerService =
        SendUtbetalingerService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            utbetalingsklient = utbetalingsklient,
        )

    private val journalførMeldekortService = JournalførMeldekortService(
        utbetalingsvedtakRepo = utbetalingsvedtakRepo,
        dokumentGateway = dokumentGateway,
    )

    private val barnMedIdentDAO = PersonopplysningerBarnMedIdentRepo()
    private val barnUtenIdentDAO = PersonopplysningerBarnUtenIdentRepo()
    private val personopplysningRepo = PostgresPersonopplysningerRepo(sessionFactory, barnMedIdentDAO, barnUtenIdentDAO)
    private val statistikkSakRepo = StatistikkSakRepoImpl(sessionFactory)
    private val statistikkStønadRepo = StatistikkStønadRepoImpl(sessionFactory)
    private val barnetilleggDAO = BarnetilleggDAO()
    private val søknadTiltakDAO = SøknadTiltakDAO()
    private val vedleggDAO = VedleggDAO()
    private val søknadDAO =
        SøknadDAO(
            barnetilleggDAO = barnetilleggDAO,
            tiltakDAO = søknadTiltakDAO,
            vedleggDAO = vedleggDAO,
        )
    private val søknadRepo = PostgresSøknadRepo(sessionFactory = sessionFactory, søknadDAO = søknadDAO)
    private val behandlingRepo =
        PostgresBehandlingRepo(
            sessionFactory = sessionFactory,
            søknadDAO = søknadDAO,
        )
    private val rammevedtakRepo =
        RammevedtakRepoImpl(
            behandlingRepo = behandlingRepo,
            sessionFactory = sessionFactory,
        )
    private val opprettUtbetalingsvedtak =
        OpprettUtbetalingsvedtakService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            rammevedtakRepo = rammevedtakRepo,
        )

    private val sakRepo =
        PostgresSakRepo(
            personopplysningerRepo = personopplysningRepo,
            behandlingRepo = behandlingRepo,
            vedtakDAO = rammevedtakRepo,
            sessionFactory = sessionFactory,
        )

    private val saksoversiktRepo =
        SaksoversiktPostgresRepo(
            sessionFactory = sessionFactory,
        )

    @Suppress("unused")
    private val rammevedtakService = RammevedtakServiceImpl(rammevedtakRepo)

    @Suppress("unused")
    private val personopplysningServiceImpl = PersonopplysningServiceImpl(personopplysningRepo)
    private val søknadService = SøknadServiceImpl(søknadRepo)
    private val meldekortRepo =
        MeldekortRepoImpl(
            sessionFactory = sessionFactory,
        )
    private val behandlingService =
        BehandlingServiceImpl(
            behandlingRepo = behandlingRepo,
            vedtakRepo = rammevedtakRepo,
            personopplysningRepo = personopplysningRepo,
            brevPublisherGateway = brevPublisherGateway,
            meldekortRepo = meldekortRepo,
            sakRepo = sakRepo,
            sessionFactory = sessionFactory,
            saksoversiktRepo = saksoversiktRepo,
            statistikkSakRepo = statistikkSakRepo,
            statistikkStønadRepo = statistikkStønadRepo,
        )
    private val sakService =
        SakServiceImpl(
            sakRepo = sakRepo,
            søknadRepo = søknadRepo,
            behandlingRepo = behandlingRepo,
            behandlingService = behandlingService,
            personGateway = personGateway,
            skjermingGateway = skjermingGateway,
            tiltakGateway = tiltakGateway,
            sessionFactory = sessionFactory,
            statistikkSakRepo = statistikkSakRepo,
        )
    private val kvpVilkårService =
        KvpVilkårServiceImpl(
            behandlingService = behandlingService,
            behandlingRepo = behandlingRepo,
        )
    private val livsoppholdVilkårService =
        LivsoppholdVilkårServiceImpl(
            behandlingService = behandlingService,
            behandlingRepo = behandlingRepo,
        )

    private val tilgangsstyringService = TilgangsstyringServiceImpl.create(skjermingBaseUrl = Configuration.skjermingClientConfig().baseUrl, getPdlPipToken = tokenProviderPdl::getToken, pdlPipUrl = Configuration.pdlClientConfig().baseUrl, getSkjermingToken = tokenProviderSkjerming::getToken)
    private val hentMeldekortService = HentMeldekortService(meldekortRepo = meldekortRepo, sakService = sakService, tilgangsstyringService = tilgangsstyringService)

    private val
    iverksettMeldekortService = IverksettMeldekortService(meldekortRepo = meldekortRepo, hentMeldekortService = hentMeldekortService, sessionFactory = sessionFactory, rammevedtakRepo = rammevedtakRepo)

    private val sendMeldekortTilBeslutterService = SendMeldekortTilBeslutterService(meldekortRepo = meldekortRepo)

    private val runCheckFactory =
        if (Configuration.isNais()) {
            RunCheckFactory(
                leaderPodLookup =
                LeaderPodLookupClient(
                    electorPath = Configuration.electorPath(),
                    logger = KotlinLogging.logger { },
                ),
            )
        } else {
            RunCheckFactory(
                leaderPodLookup =
                object : LeaderPodLookup {
                    override fun amITheLeader(localHostName: String): Either<LeaderPodLookupFeil, Boolean> = true.right()
                },
            )
        }

    private val stoppableTasks =
        TaskExecutor.startJob(
            runCheckFactory = runCheckFactory,
            tasks =
            listOf { correlationId ->
                opprettUtbetalingsvedtak.opprettUtbetalingsvedtak()
                sendUtbetalingerService.send(correlationId)
                journalførMeldekortService.send(correlationId)
            },
        )

    init {
        rapidsConnection.register(this)
    }

    fun start() {
        rapidsConnection.start()
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        log.info("Shutdown")
        stoppableTasks.stop()
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        log.info("Skal kjøre flyway migrering")
        flywayMigrate(dataSource)
        log.info("Har kjørt flyway migrering")
    }
}
