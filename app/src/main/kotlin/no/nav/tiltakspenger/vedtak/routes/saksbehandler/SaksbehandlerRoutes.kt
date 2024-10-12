package no.nav.tiltakspenger.vedtak.routes.saksbehandler

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler

internal const val SAKSBEHANDLER_PATH = "/saksbehandler"

internal fun Route.saksbehandlerRoutes(
    tokenService: TokenService,
) {
    get(SAKSBEHANDLER_PATH) {
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            // TODO post-mvp jah: Bør ikke serialisere domeneobjekt direkte til json. Bør lage en DTO for saksbehandler.
            call.respond(message = saksbehandler, status = HttpStatusCode.OK)
        }
    }
}
