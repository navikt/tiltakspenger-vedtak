package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider

const val innsendingUtdatertRoute = "/rivers/innsendingutdatert"

private val LOG = KotlinLogging.logger {}

data class InnsendingUtdatert(val journalpostId: String)

fun Route.innsendingUtdatertRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
    innsendingMediator: InnsendingMediator,
    behandlingService: BehandlingService,
) {
    post(innsendingUtdatertRoute) {
        LOG.info { "Vi har mottatt InnsendingUtdatert fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val innsendingUtdatert = call.receive<InnsendingUtdatert>()
        val innsendingUtdatertHendelse = InnsendingUtdatertHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = innsendingUtdatert.journalpostId,
        )
        innsendingMediator.håndter(innsendingUtdatertHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }

    post("$innsendingUtdatertRoute/{behandlingId}") {
        LOG.info { "Vi har mottatt melding om oppfriskning av fakta" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.hentBehandling(behandlingId)?.let {
            val innsendingUtdatertHendelse = InnsendingUtdatertHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = it.søknad().journalpostId,
            )
            innsendingMediator.håndter(innsendingUtdatertHendelse)
        } ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
