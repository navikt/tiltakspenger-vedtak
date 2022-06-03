package no.nav.tiltakspenger.vedtak

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tiltakspenger.vedtak.routes.naisRoutes
import no.nav.tiltakspenger.vedtak.routes.sak.sakRoutes
import java.time.Clock

private val LOG = KotlinLogging.logger {}
const val PORT = 8080

fun main() {
    LOG.error { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }

    val server = embeddedServer(Netty, PORT) {
        tiltakspenger()
    }.start(wait = true)

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


    RapidApplication.create(mapOf(
        "RAPID_APP_NAME" to "tiltakspenger-vedtak",
        "KAFKA_BROKERS" to "localhost:9092",
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-vedtak-v1"
    )).also {
        TestService(it)
        Thread.sleep(10000)
        LOG.error { "vi sender en melding" }
        it.publish("""{ 
            "@behov" : [ "test" ],
            "@id" : "test" }
            """.trimMargin())

    }.start()
    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.error { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
        }
    )
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
}
fun Application.tiltakspenger(
    clock: Clock = Clock.systemUTC(),
) {
    naisRoutes()

    routing {
        sakRoutes()
    }
}
