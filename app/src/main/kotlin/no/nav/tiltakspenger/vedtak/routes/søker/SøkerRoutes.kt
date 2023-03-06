package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerIdDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val innsendingHashPath = "/innsending/hash"
internal const val søkerPath = "/soker"

data class PersonIdent(
    val ident: String,
)

data class SøknadId(
    val søknadId: String,
)

data class InnsendingHash(
    val søknadId: String,
    val hash: String,
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

    get("$søkerPath/{søkerId}") {
        LOG.debug("Mottatt request på $søkerPath")
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

    post(innsendingHashPath) {
        LOG.debug("Mottatt request på $innsendingHashPath")
        val søknadId = call.receive<SøknadId>()

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val hash: String =
            try {
                søkerService.finnHashForInnsending(søknadId.søknadId)
                    ?: return@post call.respond(message = "Innsending ikke funnet", status = HttpStatusCode.NotFound)
            } catch (tex: TilgangException) {
                LOG.warn("Saksbehandler $saksbehandler har ikke tilgang", tex)
                return@post call.respond(message = "Saksbehandler har ikke tilgang", status = HttpStatusCode.Forbidden)
            }
        call.respond(
            message = InnsendingHash(søknadId = søknadId.søknadId, hash = hash),
            status = HttpStatusCode.OK,
        )
    }
}
