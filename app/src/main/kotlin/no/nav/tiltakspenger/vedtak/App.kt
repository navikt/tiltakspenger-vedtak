package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.routes.TokenVerificationConfig
import no.nav.tiltakspenger.vedtak.routes.vedtakApi

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")

    val LOG = KotlinLogging.logger {}
    LOG.info { "sjekk at denne blir maskert : 12345678901" }
    LOG.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }

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

            Thread.sleep(5000)
            it.register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    if ((System.getenv("NAIS_CLUSTER_NAME")).equals("dev-gcp")) {
                        sendPersonBehovTestMessage(rapidsConnection)
                        sendYtelserBehovTestMessage(rapidsConnection)
                        sendTiltakBehovTestMessage(rapidsConnection)
                        sendSkjermingBehovTestMessage(rapidsConnection)
                    }
                }
            })
        }.start()
    LOG.info { "nå er vi i gang" }
}

fun sendPersonBehovTestMessage(connection: RapidsConnection) {
    val LOG = KotlinLogging.logger {}
    LOG.info { "blir denne også maskert? : 12345678901" }
    LOG.info { "vi sender en person behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["person"],
            "@id" : "test",
            "@behovId": "behovId",
            "identer":[{"id":"04078309135","type":"fnr","historisk":false}]
            }"""
    connection.publish(
        json.trimMargin()
    )
    LOG.info { "vi sendte en person behovsmelding" }
}

fun sendYtelserBehovTestMessage(connection: RapidsConnection) {
    val LOG = KotlinLogging.logger {}
    LOG.info { "vi sender en ytelser behovsmelding" }
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
    LOG.info { "vi sendte en ytelser behovsmelding" }
}

fun sendTiltakBehovTestMessage(connection: RapidsConnection) {
    val LOG = KotlinLogging.logger {}
    LOG.info { "vi sender en tiltak behovsmelding" }
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
    LOG.info { "vi sendte en tiltak behovsmelding" }
}

fun sendSkjermingBehovTestMessage(connection: RapidsConnection) {
    val LOG = KotlinLogging.logger {}
    LOG.info { "vi sender en skjerming behovsmelding" }
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
    LOG.info { "vi sendte en skjerming behovsmelding" }
}
