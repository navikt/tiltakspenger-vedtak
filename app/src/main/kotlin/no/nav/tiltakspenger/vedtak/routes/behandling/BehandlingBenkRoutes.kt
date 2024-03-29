package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.routes.behandling.BehandlingDTOMapper.mapBehandlinger
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    søkerService: SøkerService,
) {
    get(behandlingerPath) {
        SECURELOG.debug("Mottatt request på $behandlingerPath")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val behandlinger = behandlingService.hentAlleBehandlinger(saksbehandler)
            .mapBehandlinger()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/startbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.taBehandling(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    get("$behandlingerPath/hentForIdent/{søkerId}") {
        SECURELOG.debug { "Mottatt request om å hente alle behandlinger for en ident" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val søkerId = SøkerId.fromString(call.parameter("søkerId"))
        val ident = søkerService.hentIdent(søkerId, saksbehandler)

        val behandlinger = behandlingService.hentBehandlingForIdent(ident, saksbehandler)
            .mapBehandlinger()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }
}
