package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.exceptions.ManglendeJWTTokenException

interface InnloggetSaksbehandlerProvider {
    fun hentSaksbehandler(principal: JWTPrincipal): Saksbehandler
    fun hentInnloggetSaksbehandler(call: ApplicationCall): Saksbehandler? {
        val principal = call.principal<JWTPrincipal>() ?: return null
        return hentSaksbehandler(principal)
    }

    fun krevInnloggetSaksbehandler(call: ApplicationCall): Saksbehandler {
        val principal = call.principal<JWTPrincipal>() ?: throw ManglendeJWTTokenException()
        return hentSaksbehandler(principal)
    }
}
