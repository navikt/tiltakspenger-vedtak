package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import java.time.LocalDate

val dayHasBegunRoute = "/rivers/passageoftime/dayhasbegun"

private val LOG = KotlinLogging.logger {}

data class DayHasBegunEvent(val date: LocalDate)

fun Route.passageOfTimeRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
    sakService: SakService,
) {
    post(dayHasBegunRoute) {
        LOG.info { "Vi har mottatt DayHasBegun fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.krevInnloggetSystembruker(call)
        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        sakService.resettLøpenr()

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
