package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.SøkerRepositoryBuilder
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.ArenaYtelserMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.PersonopplysningerMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SkjermingMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SøknadMottattRiver
import no.nav.tiltakspenger.vedtak.routes.vedtakApi
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")

    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")
    log.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }

    RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
    )
        .withKtorModule(
            vedtakApi(
                config = Configuration.TokenVerificationConfig(),
                innloggetBrukerProvider = InnloggetBrukerProvider(),
            )
        )
        .build()
        .also {
            val søkerMediator = SøkerMediator(
                søkerRepository = SøkerRepositoryBuilder.build(),
                rapidsConnection = it,
                observatører = listOf()
            )
            SøknadMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            PersonopplysningerMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            SkjermingMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            ArenaTiltakMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            ArenaYtelserMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
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
