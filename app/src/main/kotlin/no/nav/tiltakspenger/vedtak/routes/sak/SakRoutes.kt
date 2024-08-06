package no.nav.tiltakspenger.vedtak.routes.sak

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val sakPath = "/sak"

fun Sak.toDTO() = SakDTO(
    saksnummer = this.saksnummer.verdi,
    ident = this.fnr.verdi,
)

fun Route.sakRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    sakService: SakService,
) {
    get("$sakPath/{saksnummer}") {
        LOG.debug("Mottatt request på $sakPath/{saksnummer}")
        val saksnummer = call.parameter("saksnummer")
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val sakDTO = sakService.hentForSaksnummer(Saksnummer(saksnummer), saksbehandler).toDTO()
        call.respond(message = sakDTO, status = HttpStatusCode.OK)
    }
}
