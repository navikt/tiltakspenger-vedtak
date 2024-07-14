package no.nav.tiltakspenger.vedtak.routes.rivers.søknad

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
const val søknadpath = "/rivers/soknad"

fun Route.søknadRoutes(
    sakService: SakService,
) {
    post(søknadpath) {
        LOG.info { "Vi har mottatt søknad fra river" }
        val søknadDTO = call.receive<SøknadDTO>()

        // Oppretter sak med søknad og lagrer den
        sakService.motta(
            søknad = SøknadDTOMapper.mapSøknad(
                dto = søknadDTO,
                innhentet = søknadDTO.opprettet,
            ),
        )

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
