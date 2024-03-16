package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider

const val innsendingUtdatertRoute = "/rivers/innsendingutdatert"

private val LOG = KotlinLogging.logger {}

data class InnsendingUtdatert(val journalpostId: String)

fun Route.innsendingUtdatertRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
    innsendingMediator: InnsendingMediator,
) {
    post(innsendingUtdatertRoute) {
        LOG.info { "Vi har mottatt InnsendingUtdatert fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.krevInnloggetSystembruker(call)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val innsendingUtdatert = call.receive<InnsendingUtdatert>()
        val innsendingUtdatertHendelse = InnsendingUtdatertHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = innsendingUtdatert.journalpostId,
        )
        innsendingMediator.håndter(innsendingUtdatertHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
