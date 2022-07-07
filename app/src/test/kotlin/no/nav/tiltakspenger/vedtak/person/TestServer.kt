package no.nav.tiltakspenger.vedtak.person

import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.tiltakspenger.vedtak.routes.auth
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.routes.openAPI
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes

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
        openAPI()
        jacksonSerialization()
        routing {
            apiRouting {
                auth {
                    personRoutes()
                }
            }
            get("/openapi.json") {
                call.respond(this@routing.application.openAPIGen.api.serialize())
            }
            get("/") {
                call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
            }
        }
    }
}
