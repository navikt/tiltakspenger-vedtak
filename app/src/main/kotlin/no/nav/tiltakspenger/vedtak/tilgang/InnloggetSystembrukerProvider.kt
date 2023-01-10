package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker

interface InnloggetSystembrukerProvider {
    fun hentSystembruker(principal: JWTPrincipal): Systembruker
    fun hentInnloggetSystembruker(call: ApplicationCall): Systembruker? {
        val principal = call.principal<JWTPrincipal>() ?: return null
        return hentSystembruker(principal)
    }
}
