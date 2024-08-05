package no.nav.tiltakspenger.vedtak.routes.rivers.søknad

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.saksbehandling.service.SøknadService

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
const val søknadpath = "/rivers/soknad"

fun Route.søknadRoutes(
    søknadService: SøknadService,
) {
    post(søknadpath) {
        LOG.debug { "Mottatt ny søknad. Prøver deserialisere og lagre." }
        try {
            val søknadDTO = call.receive<SøknadDTO>()
            LOG.debug { "Deserialisert søknad OK med id ${søknadDTO.søknadId}" }
            // Oppretter sak med søknad og lagrer den
            søknadService.nySøknad(
                søknad = SøknadDTOMapper.mapSøknad(
                    dto = søknadDTO,
                    innhentet = søknadDTO.opprettet,
                ),
            )
            call.respond(message = "OK", status = HttpStatusCode.OK)
        } catch (exception: Exception) {
            LOG.error(
                "Feil ved mottak av søknad. Se sikkerlogg for detaljer",
                RuntimeException("Trigger en exception for å få stracktrace."),
            )
            SECURELOG.error("Feil ved mottak av søknad.", exception)
            call.respond(message = "Feil ved mottak av søknad", status = HttpStatusCode.InternalServerError)
        }
    }
}
