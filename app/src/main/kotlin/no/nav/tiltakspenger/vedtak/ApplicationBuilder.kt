package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepositoryBuilder
import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingServiceImpl
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
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
                eventMediator = eventMediator,
            )
        }
        .build()

    val innsendingRepository = InnsendingRepositoryBuilder.build()
    private val søkerRepository = SøkerRepository()
    private val behandlingRepo = PostgresBehandlingRepo()
    private val sakRepo = PostgresSakRepo()
    private val søkerService = SøkerServiceImpl(søkerRepository, innsendingRepository)
    private val sakService = SakServiceImpl(sakRepo = sakRepo, behandlingRepo = behandlingRepo)
    private val behandlingService = BehandlingServiceImpl(behandlingRepo)

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
