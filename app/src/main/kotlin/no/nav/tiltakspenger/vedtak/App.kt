package no.nav.tiltakspenger.vedtak

import io.ktor.server.application.*
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.vedtak.routes.sak.TokenVerificationConfig
import no.nav.tiltakspenger.vedtak.routes.sak.vedtakApi

private val LOG = KotlinLogging.logger {}

fun main() {
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
            TestService(it)
            Thread.sleep(5000)
            it.register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    sendPersonBehovTestMessage(rapidsConnection)
                    sendYtelserBehovTestMessage(rapidsConnection)
                    sendTiltakBehovTestMessage(rapidsConnection)
                }
            })
        }.start()
    LOG.info { "nå er vi i gang" }
}

fun sendPersonBehovTestMessage(connection: RapidsConnection) {
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
    LOG.info { "vi sender en tiltak behovsmelding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["tiltak"],
            "@id" : "test",
            "@behovId": "behovId",
            "ident": "05906398291",
            "fom" : "2019-10-01",
            "tom" : "2022-06-01"
            }"""
    connection.publish(
        json.trimMargin()
    )
    LOG.info { "vi sendte en tiltak behovsmelding" }
}
