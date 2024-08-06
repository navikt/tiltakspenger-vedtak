package no.nav.tiltakspenger.vedtak.routes.saksbehandler

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

internal const val SAKSBEHANDLER_PATH = "/saksbehandler"

fun Route.saksbehandlerRoutes(innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider) {
    get(SAKSBEHANDLER_PATH) {
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        call.respond(message = saksbehandler, status = HttpStatusCode.OK)
    }
}
