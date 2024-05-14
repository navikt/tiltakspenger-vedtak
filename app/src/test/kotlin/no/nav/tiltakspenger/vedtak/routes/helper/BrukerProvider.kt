package no.nav.tiltakspenger.vedtak.routes.helper

import io.ktor.server.application.ApplicationCall
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider

object InnloggetSystembrukerUtenRollerProvider : InnloggetSystembrukerProvider {

    override fun hentSystembruker(principal: io.ktor.server.auth.jwt.JWTPrincipal): Systembruker = systembruker()

    override fun hentInnloggetSystembruker(call: ApplicationCall): Systembruker? = systembruker()

    override fun krevInnloggetSystembruker(call: ApplicationCall): Systembruker = systembruker()

    fun systembruker() = Systembruker("testbruker", emptyList())
}

object InnloggetSaksbehandlerUtenRollerProvider : InnloggetSaksbehandlerProvider {
    override fun hentInnloggetSaksbehandler(call: ApplicationCall): Saksbehandler? = saksbehandler()

    override fun krevInnloggetSaksbehandler(call: ApplicationCall): Saksbehandler = saksbehandler()

    override fun hentSaksbehandler(principal: io.ktor.server.auth.jwt.JWTPrincipal): Saksbehandler = saksbehandler()

    fun saksbehandler() = Saksbehandler("TEST123", "Test Testesen", "test.testesen@nav.no", emptyList())
}
