package no.nav.tiltakspenger.vedtak.routes.rivers


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.EventMediator
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import java.time.LocalDate

val dayHasBegunRoute = "/rivers/passageoftime/dayhasbegun"

private val LOG = KotlinLogging.logger {}

data class DayHasBegunEvent(val date: LocalDate)

fun Route.passageOfTimeRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
    eventMediator: EventMediator,
) {
    post("$dayHasBegunRoute") {
        LOG.info { "Vi har mottatt DayHasBegun fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val dayHasBegun = call.receive<DayHasBegunEvent>()
        eventMediator.håndter(dayHasBegun)

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
