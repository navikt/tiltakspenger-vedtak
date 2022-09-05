package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.ArenaYtelserMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.PersonopplysningerMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SkjermingMottattRiver
import no.nav.tiltakspenger.vedtak.rivers.SøknadMottattRiver
import no.nav.tiltakspenger.vedtak.routes.TokenVerificationConfig
import no.nav.tiltakspenger.vedtak.routes.vedtakApi

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
        RapidApplication.RapidApplicationConfig.fromEnv(
            mapOf(
                "RAPID_APP_NAME" to "tiltakspenger-vedtak",
                "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
                "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
                "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
                "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
                "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
                "KAFKA_RESET_POLICY" to "latest",
                "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-vedtak-v1"
            )
        )
    )
        .withKtorModule(vedtakApi(TokenVerificationConfig.fromEnv()))
        .build()
        .also {
            val søkerRepository = PostgresSøkerRepository
            val søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
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
                        flywayMigrate()
                    }
                }
            )
        }.start()
    log.info { "nå er vi i gang" }
}
