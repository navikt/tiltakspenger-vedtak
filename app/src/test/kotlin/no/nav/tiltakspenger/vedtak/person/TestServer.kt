package no.nav.tiltakspenger.vedtak.person

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import no.nav.tiltakspenger.vedtak.routes.TokenVerificationConfig
import no.nav.tiltakspenger.vedtak.routes.vedtakApi

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        vedtakApi(TokenVerificationConfig("", "", "", 1000))()
    }.start(wait = true)
}