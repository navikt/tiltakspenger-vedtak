package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerIdDTO
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val søkerPath = "/soker"

data class PersonIdentDTO(
    val ident: String,
)

fun Route.søkerRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    søkerService: SøkerService,
) {
    post(søkerPath) {
        LOG.debug("Mottatt request på $søkerPath")
        val personIdent = call.receive<PersonIdentDTO>()
        call.auditHvisInnlogget(berørtBruker = personIdent.ident)

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val response: SøkerIdDTO =
            søkerService.hentSøkerIdOrNull(Fnr.fromString(personIdent.ident), saksbehandler)
                ?: return@post call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)

        call.respond(message = response, status = HttpStatusCode.OK)
    }
}
