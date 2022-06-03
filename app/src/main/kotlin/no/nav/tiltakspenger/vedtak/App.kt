package no.nav.tiltakspenger.vedtak

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tiltakspenger.vedtak.routes.naisRoutes
import no.nav.tiltakspenger.vedtak.routes.sak.sakRoutes
import org.intellij.lang.annotations.Language
import java.time.Clock

private val LOG = KotlinLogging.logger {}
const val PORT = 8080

fun main() {
    LOG.error { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
//    val
//    val con = RapidApplication.Builder(
//        RapidApplication.RapidApplicationConfig.fromEnv(
//            mapOf(
//                "DB_DATABASE" to "dp-mottak",
//                "DB_HOST" to "localhost",
//                "DB_PASSWORD" to "password",
//                "DB_PORT" to "5432",
//                "DB_USERNAME" to "username",
//                "DP_PROXY_SCOPE" to "api://dev-fss.teamdagpenger.dp-proxy/.default",
//                "DP_PROXY_URL" to "https://dp-proxy.dev-fss-pub.nais.io",
//                "HTTP_PORT" to "8080",
//                "KAFKA_CONSUMER_GROUP_ID" to "dp-mottak-v1",
//                "KAFKA_EXTRA_TOPIC" to "teamdagpenger.mottak.v1,teamdagpenger.regel.v1",
//                "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
//                "KAFKA_RESET_POLICY" to "latest",
//                "PDL_API_SCOPE" to "api://dev-fss.pdl.pdl-api/.default",
//                "PDL_API_URL" to "https://pdl-api.dev-fss-pub.nais.io",
//                "UNLEASH_URL" to "https://unleash.nais.io/api/"
//            )
//        )
//    )


    RapidApplication.create(
        mapOf(
            "RAPID_APP_NAME" to "tiltakspenger-vedtak",
            "KAFKA_BROKERS" to "localhost:9092",
            "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
            "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
            "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
            "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-vedtak-v1"
        )
    ).also {
        TestService(it)
        Thread.sleep(10000)
        LOG.error { "vi sender en melding" }
        // language=JSON
        val json = """
            { 
            "@behov" : [ "test" ],
            "@id" : "test"
            }"""
        try {
            it.publish(
                json.trimMargin()
            )
        } catch (e: Exception) {
            LOG.error { "En feil oppstod: $e" }
        }
        LOG.error { "vi sendte en melding" }

    }.start()

}

internal class TestService(rapidsConnection: RapidsConnection) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("test"))
                it.requireKey("@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.error { packet }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        LOG.error { problems }
    }
}

fun Application.tiltakspenger(
    clock: Clock = Clock.systemUTC(),
) {
    naisRoutes()

    routing {
        sakRoutes()
    }
}
