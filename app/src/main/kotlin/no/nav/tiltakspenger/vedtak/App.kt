package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

private val LOG = KotlinLogging.logger {}

fun main() {
    LOG.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
    RapidApplication.create(
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
    ).also {
        TestService(it)
        Thread.sleep(5000)
        it.register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                sendPersonBehovTestMessage(rapidsConnection)
                sendYtelserBehovTestMessage(rapidsConnection)
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
            "identer":[{"id":"01017912345","type":"fnr","historisk":false}]
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
            "ident": "05906398291"
            }"""
    connection.publish(
        json.trimMargin()
    )
    LOG.info { "vi sendte en ytelser behovsmelding" }
}
