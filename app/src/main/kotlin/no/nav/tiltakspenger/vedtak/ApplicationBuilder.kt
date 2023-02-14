package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepositoryBuilder
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSystembrukerProvider

val log = KotlinLogging.logger {}
val securelog = KotlinLogging.logger("tjenestekall")

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    val rapidsConnection: RapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers),
    )
        .withKtorModule {
            vedtakApi(
                config = Configuration.TokenVerificationConfig(),
                innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                innloggetSystembrukerProvider = JWTInnloggetSystembrukerProvider(),
                søkerService = søkerService,
                innsendingMediator = innsendingMediator,
                søkerMediator = søkerMediator,
                innsendingAdminService = innsendingAdminService,
                eventMediator = eventMediator,
            )
        }
        .build()

    val innsendingRepository = InnsendingRepositoryBuilder.build()
    private val søkerRepository = SøkerRepository()
    private val søkerService = SøkerServiceImpl(søkerRepository, innsendingRepository)

    val innsendingMediator = InnsendingMediator(
        innsendingRepository = innsendingRepository,
        rapidsConnection = rapidsConnection,
        observatører = listOf(),
    )
    private val søkerMediator = SøkerMediator(
        søkerRepository = søkerRepository,
        rapidsConnection = rapidsConnection,
    )
    val innsendingAdminService = InnsendingAdminService(
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
//        SøknadMottattRiver(
//            innsendingMediator = innsendingMediator,
//            søkerMediator = søkerMediator,
//            rapidsConnection = rapidsConnection,
//        )
//        PersonopplysningerMottattRiver(
//            innsendingMediator = innsendingMediator,
//            søkerMediator = søkerMediator,
//            rapidsConnection = rapidsConnection,
//        )
//        SkjermingMottattRiver(
//            innsendingMediator = innsendingMediator,
//            søkerMediator = søkerMediator,
//            rapidsConnection = rapidsConnection,
//        )
//        ArenaTiltakMottattRiver(
//            innsendingMediator = innsendingMediator,
//            rapidsConnection = rapidsConnection,
//        )
//        ArenaYtelserMottattRiver(
//            innsendingMediator = innsendingMediator,
//            rapidsConnection = rapidsConnection,
//        )
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
