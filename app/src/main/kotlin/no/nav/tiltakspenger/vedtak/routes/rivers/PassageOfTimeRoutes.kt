package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import java.time.LocalDate

val dayHasBegunRoute = "/rivers/passageoftime/dayhasbegun"

private val LOG = KotlinLogging.logger {}

data class DayHasBegunEvent(val date: LocalDate)

fun Route.passageOfTimeRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
) {
    post(dayHasBegunRoute) {
        LOG.info { "Vi har mottatt DayHasBegun fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        // Vi skal slutte å trigge oppdatering av alle innsendinger hver natt
        // val dayHasBegun = call.receive<DayHasBegunEvent>()
        // eventMediator.håndter(dayHasBegun)

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
