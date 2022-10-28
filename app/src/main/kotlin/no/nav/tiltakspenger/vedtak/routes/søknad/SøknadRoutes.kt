package no.nav.tiltakspenger.vedtak.routes.søknad

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.vedtak.audit.auditHvisInnlogget
import no.nav.tiltakspenger.vedtak.service.StorSøknadDTO
import no.nav.tiltakspenger.vedtak.service.SøknadService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

internal const val søknadPath = "/person/søknad"

data class SøknadBody(
    val ident: String
)

fun Route.søknadRoutes(
    innloggetBrukerProvider: InnloggetBrukerProvider,
    søknadService: SøknadService,
) {
    route("$søknadPath/{søknadId}") {
        post {
            val søknadId: SøknadId = call.parameters["søknadId"]?.let {
                SøknadId.fromDb(it)
            } ?: return@post call.respond(message = "Må oppgi en SøknadId", status = HttpStatusCode.NotFound)

            val ident = call.receive<SøknadBody>().ident

            call.auditHvisInnlogget(berørtBruker = ident)

            val response: StorSøknadDTO? = søknadService.hentSøknad(ident, søknadId)
            if (response == null) {
                call.respond(message = "Søknad ikke funnet", status = HttpStatusCode.NotFound)
            } else {
                call.respond(message = response, status = HttpStatusCode.OK)
            }
        }
    }
}
