package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerIdDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val søknaderPath = "/person/soknader"
internal const val søkerPath = "/soker"

data class PersonIdent(
    val ident: String
)

fun Route.søkerRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    søkerService: SøkerService,
) {

    post(søkerPath) {
        LOG.debug("Mottatt request på $søkerPath")
        val personIdent = call.receive<PersonIdent>()
        call.auditHvisInnlogget(berørtBruker = personIdent.ident)

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val response: SøkerIdDTO =
            try {
                søkerService.hentSøkerId(personIdent.ident, saksbehandler)
                    ?: return@post call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)
            } catch (tex: TilgangException) {
                LOG.warn("Saksbehandler har ikke tilgang", tex)
                return@post call.respond(message = "Saksbehandler har ikke tilgang", status = HttpStatusCode.Forbidden)
            }
        call.respond(message = response, status = HttpStatusCode.OK)
    }

    get("$søknaderPath/{sokerId}") {
        LOG.debug("Mottatt request på $søknaderPath")
        val søkerId = call.parameters["sokerId"]?.let { SøkerId.fromDb(it) }
            ?: return@get call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)
        // TODO:
        // call.auditHvisInnlogget(berørtBruker = personIdent.ident)

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val response: SøkerDTO =
            try {
                søkerService.hentSøkerOgSøknader(søkerId, saksbehandler)
                    ?: return@get call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)
            } catch (tex: TilgangException) {
                LOG.warn("Saksbehandler har ikke tilgang", tex)
                return@get call.respond(message = "Saksbehandler har ikke tilgang", status = HttpStatusCode.Forbidden)
            }
        call.respond(message = response, status = HttpStatusCode.OK)
    }
}
