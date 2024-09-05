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
import no.nav.tiltakspenger.vedtak.context.ApplicationContext
import no.nav.tiltakspenger.vedtak.db.DataSourceSetup
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.jobber.TaskExecutor
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

    private val dataSource = DataSourceSetup.createDatasource()
    private val sessionCounter = SessionCounter(log)
    private val sessionFactory = PostgresSessionFactory(dataSource, sessionCounter)
    private val applicationContext = ApplicationContext.create(sessionFactory)
    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .Builder(rapidConfig)
            .withKtorModule {
                vedtakApi(
                    config = Configuration.TokenVerificationConfig(),
                    innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                    sakService = applicationContext.sakContext.sakService,
                    behandlingService = applicationContext.førstegangsbehandlingContext.behandlingService,
                    rammevedtakService = applicationContext.førstegangsbehandlingContext.rammevedtakService,
                    kvpVilkårService = applicationContext.førstegangsbehandlingContext.kvpVilkårService,
                    livsoppholdVilkårService = applicationContext.førstegangsbehandlingContext.livsoppholdVilkårService,
                    søknadService = applicationContext.søknadContext.søknadService,
                    hentUtbetalingsvedtakService = applicationContext.utbetalingContext.hentUtbetalingsvedtakService,
                    hentMeldekortService = applicationContext.meldekortContext.hentMeldekortService,
                    iverksettMeldekortService = applicationContext.meldekortContext.iverksettMeldekortService,
                    sendMeldekortTilBeslutterService = applicationContext.meldekortContext.sendMeldekortTilBeslutterService,
                )
            }.build()

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
                    override fun amITheLeader(localHostName: String): Either<LeaderPodLookupFeil, Boolean> =
                        true.right()
                },
            )
        }

    private val stoppableTasks =
        TaskExecutor.startJob(
            runCheckFactory = runCheckFactory,
            tasks =
            listOf { correlationId ->
                applicationContext.utbetalingContext.opprettUtbetalingsvedtakService.opprettUtbetalingsvedtak()
                applicationContext.utbetalingContext.sendUtbetalingerService.send(correlationId)
                applicationContext.utbetalingContext.journalførUtbetalingsvedtakService.send(correlationId)
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
