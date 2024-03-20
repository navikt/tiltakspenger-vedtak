package no.nav.tiltakspenger.vedtak.routes.sak

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val sakPath = "/sak"

fun Route.sakRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    søkerService: SøkerService,
    sakService: SakService,
) {
    get("$sakPath/hentForSokerId/{søkerId}") {
        LOG.debug("Mottatt request på $sakPath/hentForSokerId/{søkerId}")
        val søkerId = SøkerId.fromString(call.parameter("søkerId"))

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val ident = søkerService.hentIdent(søkerId, saksbehandler)
        val saker = sakService.hentForIdent(ident, saksbehandler)

        call.respond(message = saker, status = HttpStatusCode.OK)
    }
}
