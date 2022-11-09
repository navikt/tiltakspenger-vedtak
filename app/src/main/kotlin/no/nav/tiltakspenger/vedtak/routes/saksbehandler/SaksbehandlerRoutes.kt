package no.nav.tiltakspenger.vedtak.routes.saksbehandler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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