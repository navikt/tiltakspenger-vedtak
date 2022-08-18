package no.nav.tiltakspenger.vedtak

import mu.KLogger
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
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
            /*
            val søkerRepository = TODO()
            val søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = it,
                observatører = listOf()
            )
            SøknadMottakTjeneste(søkerMediator = søkerMediator, rapidsConnection = it)
            */
            TestService(it)

            log.info("Sover før vi sender test meldinger")
            Thread.sleep(5000)
            log.info("Sovet ferdig før vi sender test meldinger")
            it.register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    if ((System.getenv("NAIS_CLUSTER_NAME")).equals("dev-gcp")) {
                        log.info("Sender test meldinger")
                        sendPersonBehovTestMessage(rapidsConnection, log)
                        sendYtelserBehovTestMessage(rapidsConnection, log)
                        sendTiltakBehovTestMessage(rapidsConnection, log)
                        sendSkjermingBehovTestMessage(rapidsConnection, log)
                        sendInstitusjonBehovTestMessage(rapidsConnection, log)
                        log.info("Har sendt test meldinger")
                    }
                }
            })
        }.start()
    log.info { "nå er vi i gang" }
}

fun sendPersonBehovTestMessage(connection: RapidsConnection, log: KLogger) {
    log.info { "vi sender en person behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["persondata"],
            "@id" : "test",
            "@behovId": "behovId",
            "identer":[{"id":"04078309135","type":"fnr","historisk":false}]
            }"""
    connection.publish(
        json.trimMargin()
    )
    log.info { "vi sendte en person behovsmelding" }
}

fun sendYtelserBehovTestMessage(connection: RapidsConnection, log: KLogger) {
    log.info { "vi sender en ytelser behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["ytelser"],
            "@id" : "test",
            "@behovId": "behovId",
            "ident": "05906398291",
            "fom" : "2019-10-01",
            "tom" : "2022-06-01"
            }"""
    connection.publish(
        json.trimMargin()
    )
    log.info { "vi sendte en ytelser behovsmelding" }
}

fun sendTiltakBehovTestMessage(connection: RapidsConnection, log: KLogger) {
    log.info { "vi sender en tiltak behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["tiltak"],
            "@id" : "test",
            "@behovId": "behovId",
            "ident": "05906398291"
            }"""
    connection.publish(
        json.trimMargin()
    )
    log.info { "vi sendte en tiltak behovsmelding" }
}

fun sendSkjermingBehovTestMessage(connection: RapidsConnection, log: KLogger) {
    log.info { "vi sender en skjerming behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["skjerming"],
            "@id" : "test",
            "@behovId": "behovId",
            "ident": "05906398291",
            "fom" : "2019-10-01",
            "tom" : "2022-06-01"
            }"""
    connection.publish(
        json.trimMargin()
    )
    log.info { "vi sendte en skjerming behovsmelding" }
}

fun sendInstitusjonBehovTestMessage(connection: RapidsConnection, log: KLogger) {
    log.info { "vi sender en institusjon behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["institusjon"],
            "@id" : "test",
            "@behovId": "behovId",
            "ident": "10108000398",
            "fom" : "2019-10-01",
            "tom" : "2022-06-01"
            }"""
    connection.publish(
        json.trimMargin()
    )
    log.info { "vi sendte en institusjon behovsmelding" }
}
