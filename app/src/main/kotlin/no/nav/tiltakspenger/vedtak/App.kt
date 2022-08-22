package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.tiltakspenger.vedtak.repository.InMemorySøkerRepository
import no.nav.tiltakspenger.vedtak.rivers.PersondataMottattRiver
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
            val søkerRepository = InMemorySøkerRepository()
            val søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = it,
                observatører = listOf()
            )
            SøknadMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            PersondataMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
            SkjermingMottattRiver(søkerMediator = søkerMediator, rapidsConnection = it)
        }.start()
    log.info { "nå er vi i gang" }
}