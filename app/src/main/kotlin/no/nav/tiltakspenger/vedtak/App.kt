package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepositoryBuilder
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.ArenaYtelserMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.PersonopplysningerMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SkjermingMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SøknadMottattRiver
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider

fun main() {
    System.setProperty("logback.configurationFile", Configuration.logbackConfigurationFile())

    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")
    log.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }

    val applicationBuilder = ApplicationBuilder(System.getenv())
    applicationBuilder.start()
}


//    val innsendingRepository = InnsendingRepositoryBuilder.build()
//    val søkerRepository = SøkerRepository()
//    val søkerService = SøkerServiceImpl(søkerRepository, innsendingRepository)
//
//    val rapidsConnection: RapidsConnection = RapidApplication.Builder(
//        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
//    )
//        .withKtorModule {
//            vedtakApi(
//                config = Configuration.TokenVerificationConfig(),
//                innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
//                søkerService = søkerService,
//                innsendingMediator = innsendingMediator // hva skal vi skrive her?
//            )
//        }
//        .build()
//
//    val innsendingMediator = InnsendingMediator(
//        innsendingRepository = innsendingRepository,
//        rapidsConnection = rapidsConnection,
//        observatører = listOf()
//    )
//    val søkerMediator = SøkerMediator(
//        søkerRepository = søkerRepository,
//        rapidsConnection = rapidsConnection,
//    )
//    log.info { "nå er vi i gang" }
//}
//
//
//
//.also {
//
//    SøknadMottattRiver(
//        innsendingMediator = innsendingMediator,
//        søkerMediator = søkerMediator,
//        rapidsConnection = it,
//    )
//    PersonopplysningerMottattRiver(
//        innsendingMediator = innsendingMediator,
//        søkerMediator = søkerMediator,
//        rapidsConnection = it,
//    )
//    SkjermingMottattRiver(
//        innsendingMediator = innsendingMediator,
//        søkerMediator = søkerMediator,
//        rapidsConnection = it,
//    )
//    ArenaTiltakMottattRiver(
//        innsendingMediator = innsendingMediator,
//        rapidsConnection = it,
//    )
//    ArenaYtelserMottattRiver(
//        innsendingMediator = innsendingMediator,
//        rapidsConnection = it,
//    )
//    it.register(
//        object : RapidsConnection.StatusListener {
//            override fun onStartup(rapidsConnection: RapidsConnection) {
//                log.info("Skal kjøre flyway migrering")
//                flywayMigrate()
//                log.info("Har kjørt flyway migrering")
//            }
//        }
//    )
//}.start()
