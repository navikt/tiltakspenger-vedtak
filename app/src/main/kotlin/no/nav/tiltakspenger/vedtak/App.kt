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
    System.setProperty("logback.configurationFile", "egenLogback.xml")

    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")
    log.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }


    val innsendingRepository = InnsendingRepositoryBuilder.build()
    val søkerRepository = SøkerRepository()
    val søkerService = SøkerServiceImpl(søkerRepository, innsendingRepository)

    RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
    )
        .withKtorModule(
            vedtakApi(
                config = Configuration.TokenVerificationConfig(),
                innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                søkerService = søkerService
            )
        )
        .build()
        .also {
            val innsendingMediator = InnsendingMediator(
                innsendingRepository = innsendingRepository,
                rapidsConnection = it,
                observatører = listOf()
            )
            val søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = it,
            )
            SøknadMottattRiver(
                innsendingMediator = innsendingMediator,
                søkerMediator = søkerMediator,
                rapidsConnection = it
            )
            PersonopplysningerMottattRiver(
                innsendingMediator = innsendingMediator,
                rapidsConnection = it,
                søkerMediator = søkerMediator
            )
            SkjermingMottattRiver(innsendingMediator = innsendingMediator, rapidsConnection = it)
            ArenaTiltakMottattRiver(innsendingMediator = innsendingMediator, rapidsConnection = it)
            ArenaYtelserMottattRiver(innsendingMediator = innsendingMediator, rapidsConnection = it)
            it.register(
                object : RapidsConnection.StatusListener {
                    override fun onStartup(rapidsConnection: RapidsConnection) {
                        log.info("Skal kjøre flyway migrering")
                        flywayMigrate()
                        log.info("Har kjørt flyway migrering")
                    }
                }
            )
        }.start()
    log.info { "nå er vi i gang" }
}
