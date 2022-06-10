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
                sendTestMessage(rapidsConnection)
            }
        })
    }.start()
    LOG.info { "nå er vi i gang" }
}

fun sendTestMessage(connection: RapidsConnection) {
    LOG.info { "vi sender en melding" }
    // language=JSON
    val json = """
            { 
            "@behov" : ["person"],
            "@id" : "test",
            "@behovId": "behovId"
            }"""
    connection.publish(
        json.trimMargin()
    )
    LOG.info { "vi sendte en melding" }
}
