package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.brevpublisher.BrevPublisherGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.meldekort.MeldekortGrunnlagGatewayImpl
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingClient
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingGatewayImpl
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepositoryBuilder
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import no.nav.tiltakspenger.vedtak.repository.multi.MultiRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepositoryImpl
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepoImpl
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.service.vedtak.VedtakServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSystembrukerProvider

val log = KotlinLogging.logger {}
val securelog = KotlinLogging.logger("tjenestekall")

internal class ApplicationBuilder(@Suppress("UNUSED_PARAMETER") config: Map<String, String>) :
    RapidsConnection.StatusListener {
    private val rapidConfig = if (Configuration.applicationProfile() == Profile.LOCAL) {
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers, LokalKafkaConfig())
    } else {
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
    }
    val rapidsConnection: RapidsConnection = RapidApplication.Builder(rapidConfig)
        .withKtorModule {
            vedtakApi(
                config = Configuration.TokenVerificationConfig(),
                innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                innloggetSystembrukerProvider = JWTInnloggetSystembrukerProvider(),
                søkerService = søkerService,
                sakService = sakService,
                behandlingService = behandlingService,
                innsendingMediator = innsendingMediator,
                søkerMediator = søkerMediator,
                innsendingAdminService = innsendingAdminService,
                attesteringRepo = attesteringRepo,
            )
        }
        .build()

    val innsendingRepository = InnsendingRepositoryBuilder.build()
    private val tokenProviderUtbetaling: AzureTokenProvider =
        AzureTokenProvider(config = Configuration.oauthConfigUtbetaling())

    private val sakRepo = PostgresSakRepo()
    private val utbetalingClient = UtbetalingClient(getToken = tokenProviderUtbetaling::getToken)
    private val utbetalingGateway = UtbetalingGatewayImpl(utbetalingClient)
    private val brevPublisherGateway = BrevPublisherGatewayImpl(rapidsConnection)
    private val meldekortGrunnlagGateway = MeldekortGrunnlagGatewayImpl(rapidsConnection)
    private val utbetalingService = UtbetalingServiceImpl(utbetalingGateway, sakRepo)
    private val søkerRepository = SøkerRepositoryImpl()
    private val behandlingRepo = PostgresBehandlingRepo()
    private val saksopplysningRepo = SaksopplysningRepo()
    private val vurderingRepo = VurderingRepo()
    private val attesteringRepo = AttesteringRepoImpl()
    private val vedtakRepo = VedtakRepoImpl(behandlingRepo, saksopplysningRepo, vurderingRepo)
    private val multiRepo = MultiRepoImpl(behandlingRepo, attesteringRepo, vedtakRepo)
    private val personopplysningRepo = PostgresPersonopplysningerRepo()
    private val vedtakService = VedtakServiceImpl(vedtakRepo)
    private val søkerService = SøkerServiceImpl(søkerRepository)
    private val personopplysningServiceImpl = PersonopplysningServiceImpl(personopplysningRepo)
    private val behandlingService =
        BehandlingServiceImpl(
            behandlingRepo,
            personopplysningRepo,
            utbetalingService,
            brevPublisherGateway,
            meldekortGrunnlagGateway,
            multiRepo,
        )
    private val sakService =
        SakServiceImpl(sakRepo = sakRepo, behandlingRepo = behandlingRepo, behandlingService = behandlingService)

    val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = rapidsConnection,
        observatører = listOf(),
    )
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = rapidsConnection,
    )
    private val innsendingAdminService = InnsendingAdminService(
        innsendingRepository = innsendingRepository,
        innsendingMediator = innsendingMediator,
    )
    private val eventMediator = EventMediator(
        rapidsConnection = rapidsConnection,
        innsendingAdminService = innsendingAdminService,
    )

    init {
        AppMetrikker.antallInnsendingerLagret(innsendingRepository)
        AppMetrikker.antallInnsendingerFeilet(innsendingRepository)
        AppMetrikker.antallInnsendingerStoppetUnderBehandling(innsendingRepository)

        rapidsConnection.register(this)
    }

    fun start() {
        rapidsConnection.start()
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        log.info("Shutdown")
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        log.info("Skal kjøre flyway migrering")
        flywayMigrate()
        log.info("Har kjørt flyway migrering")
    }
}
