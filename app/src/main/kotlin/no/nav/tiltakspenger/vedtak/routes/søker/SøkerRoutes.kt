package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.søker.SøkerDTO
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

private val LOG = KotlinLogging.logger {}

internal const val søknaderPath = "/person/søknader"

data class PersonIdent(
    val ident: String
)

fun Route.søkerRoutes(
    innloggetBrukerProvider: InnloggetBrukerProvider,
    søkerService: SøkerService,
) {

    post(søknaderPath) {
        LOG.debug("Mottatt request på $søknaderPath")
        val personIdent = call.receive<PersonIdent>()
        call.auditHvisInnlogget(berørtBruker = personIdent.ident)

        val response: SøkerDTO = søkerService.hentSøkerOgSøknader(personIdent.ident)
            ?: return@post call.respond(message = "Søker ikke funnet", status = HttpStatusCode.NotFound)

        call.respond(message = response, status = HttpStatusCode.OK)
    }
}
