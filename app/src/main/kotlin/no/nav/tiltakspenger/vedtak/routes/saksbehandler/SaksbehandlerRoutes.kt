package no.nav.tiltakspenger.vedtak.routes.saksbehandler

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

internal const val saksbehandlerPath = "/saksbehandler"

fun Route.saksbehandlerRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
) {
    get(saksbehandlerPath) {
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)
        call.respond(message = saksbehandler, status = HttpStatusCode.OK)
    }
}
