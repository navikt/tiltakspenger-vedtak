package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.exceptions.ManglendeJWTTokenException

interface InnloggetSystembrukerProvider {
    fun hentSystembruker(principal: JWTPrincipal): Systembruker
    fun hentInnloggetSystembruker(call: ApplicationCall): Systembruker? {
        val principal = call.principal<JWTPrincipal>() ?: return null
        return hentSystembruker(principal)
    }

    fun krevInnloggetSystembruker(call: ApplicationCall): Systembruker {
        val principal = call.principal<JWTPrincipal>() ?: throw ManglendeJWTTokenException()
        return hentSystembruker(principal)
    }
}
