package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*

private val LOG = KotlinLogging.logger {}

fun main() {
    LOG.error { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
    val con = RapidApplication.create(
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
//        Thread.sleep(5000)
//        sendTestMessage(it)
    } //.start()
    con.start()
    Thread.sleep(5000)
    sendTestMessage(con)
    LOG.error { "nå er vi i gang" }
}

fun sendTestMessage(connection: RapidsConnection) {
    LOG.error { "vi sender en melding" }
    // language=JSON
    val json = """
            { 
            "@behov" : "test",
            "@id" : "test"
            }"""
    try {
        connection.publish(
            json.trimMargin()
        )
    } catch (e: Throwable) {
        LOG.error { "En feil oppstod: $e" }
    }
    LOG.error { "vi sendte en melding" }

}