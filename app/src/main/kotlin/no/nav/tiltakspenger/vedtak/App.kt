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
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadServiceImpl
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

    val søkerRepository = SøkerRepositoryBuilder.build()
    val søkerService = SøkerServiceImpl(søkerRepository)
    val søknadService = SøknadServiceImpl(søkerRepository)

    RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.rapidsAndRivers)
    )
        .withKtorModule(
            vedtakApi(
                config = Configuration.TokenVerificationConfig(),
                innloggetSaksbehandlerProvider = JWTInnloggetSaksbehandlerProvider(),
                søkerService = søkerService,
                søknadService = søknadService,
            )
        )
        .build()
        .also {
            val innsendingMediator = InnsendingMediator(
                innsendingRepository = søkerRepository,
                rapidsConnection = it,
                observatører = listOf()
            )
            SøknadMottattRiver(innsendingMediator = innsendingMediator, rapidsConnection = it)
            PersonopplysningerMottattRiver(innsendingMediator = innsendingMediator, rapidsConnection = it)
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
