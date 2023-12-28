package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingClient
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepositoryBuilder
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringRepoImpl
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.SaksopplysningRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.VurderingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresPersonopplysningerRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
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
    val rapidsConnection: RapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers),
    )
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
                utbetalingService = utbetalingServiceImpl,
                personopplysningService = personopplysningServiceImpl,
                attesteringRepo = attesteringRepo,
            )
        }
        .build()

    val innsendingRepository = InnsendingRepositoryBuilder.build()
    private val søkerRepository = SøkerRepository()
    private val behandlingRepo = PostgresBehandlingRepo()
    private val sakRepo = PostgresSakRepo()
    private val saksopplysningRepo = SaksopplysningRepo()
    private val vurderingRepo = VurderingRepo()
    private val personopplysningRepo = PostgresPersonopplysningerRepo()
    private val attesteringRepo = AttesteringRepoImpl()
    private val vedtakRepo = VedtakRepoImpl(behandlingRepo, saksopplysningRepo, vurderingRepo)
    private val vedtakService = VedtakServiceImpl(vedtakRepo, rapidsConnection)
    private val søkerService = SøkerServiceImpl(søkerRepository)
    private val behandlingService = BehandlingServiceImpl(behandlingRepo, vedtakService, attesteringRepo)
    private val sakService =
        SakServiceImpl(sakRepo = sakRepo, behandlingRepo = behandlingRepo, behandlingService = behandlingService)

    private val personopplysningServiceImpl = PersonopplysningServiceImpl(personopplysningRepo)
    private val tokenProviderUtbetaling: AzureTokenProvider =
        AzureTokenProvider(config = Configuration.oauthConfigUtbetaling())

    private val utbetalingClient = UtbetalingClient(getToken = tokenProviderUtbetaling::getToken)
    private val utbetalingServiceImpl = UtbetalingServiceImpl(utbetalingClient)
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
