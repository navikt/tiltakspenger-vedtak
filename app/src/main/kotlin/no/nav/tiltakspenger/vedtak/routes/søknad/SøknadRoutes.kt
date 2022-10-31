package no.nav.tiltakspenger.vedtak.routes.søknad

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val personsøknadPath = "/person/søknad"
internal const val søknadPath = "/søknad"

data class SøknadBody(
    val ident: String
)

fun Route.søknadRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    søknadService: SøknadService,
) {
    get("${søknadPath}/{søknadId}") {
        val søknadId = call.parameters["søknadId"]
            ?: return@get call.respond(message = "Mangler soknadId", status = HttpStatusCode.NotFound)
        LOG.info { "Vi har truffet GET /søknad" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val behandlingAvSøknad = try {
            søknadService.hentBehandlingAvSøknad(søknadId, saksbehandler)
                ?: return@get call.respond(message = "Søknad ikke funnet", status = HttpStatusCode.NotFound)
        } catch (tex: TilgangException) {
            LOG.warn("Saksbehandler har ikke tilgang", tex)
            return@get call.respond(message = "Saksbehandler har ikke tilgang", status = HttpStatusCode.Forbidden)
        }

        call.auditHvisInnlogget(berørtBruker = behandlingAvSøknad.personopplysninger.ident)
        call.respond(message = behandlingAvSøknad, status = HttpStatusCode.OK)
    }
}
