package no.nav.tiltakspenger.vedtak.routes.person

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.service.PersonServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

fun main() {
    embeddedServer(Netty, 8080) {
        install(CORS) {
            anyHost()
        }
        vedtakTestApi()()
    }.start(wait = true)
}

internal fun vedtakTestApi(): Application.() -> Unit {
    return {
        jacksonSerialization()
        routing {
            personRoutes(
                innloggetBrukerProvider = InnloggetBrukerProvider(),
                personService = PersonServiceImpl(
                    søkerRepository = PostgresSøkerRepository(
                        SøknadDAO()
                    )
                ),
            )
        }
    }
}
