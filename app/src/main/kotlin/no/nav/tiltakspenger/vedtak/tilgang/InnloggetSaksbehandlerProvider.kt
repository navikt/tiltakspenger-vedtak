package no.nav.tiltakspenger.vedtak.tilgang

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import no.nav.tiltakspenger.felles.Saksbehandler

interface InnloggetSaksbehandlerProvider {
    fun hentSaksbehandler(principal: JWTPrincipal): Saksbehandler
    fun hentInnloggetSaksbehandler(call: ApplicationCall): Saksbehandler? {
        val principal = call.principal<JWTPrincipal>() ?: return null
        return hentSaksbehandler(principal)
    }
}
