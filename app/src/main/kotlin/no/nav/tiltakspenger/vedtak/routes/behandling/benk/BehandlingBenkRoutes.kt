package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingerPath
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    søkerService: SøkerService,
    sakService: SakService,
) {
    get(behandlingerPath) {
        SECURELOG.debug("Mottatt request for å hente alle behandlinger på benken")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        val behandlinger = behandlingService.hentBehandlingerForBenk(saksbehandler)
            .let {
                listOf(it.behandlinger.fraBehandlingToBehandlingBenkDto(), it.søknader.fraSøknadToBehandlingBenkDto())
            }

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/startbehandling/{soknadId}") {
        SECURELOG.debug { "Mottatt request for å starte behandlingen. Knytter også saksbehandleren til behandlingen." }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val søknadId = SøknadId.fromString(call.parameter("soknadId"))

        sakService.startFørstegangsbehandling(søknadId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/tabehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.taBehandling(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    // TODO jah: Kommenterer ut denne inntil videre. Søk på fnr vil ikke lengre fungere for frontend
//    get("$behandlingerPath/hentForIdent/{søkerId}") {
//        SECURELOG.debug { "Mottatt request om å hente alle behandlinger for en ident" }
//
//        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
//        val søkerId = SøkerId.fromString(call.parameter("søkerId"))
//        val ident = søkerService.hentIdent(søkerId, saksbehandler)
//
//        val behandlinger = behandlingService.hentBehandlingForIdent(ident, saksbehandler)
//            .mapBehandlinger()
//
//        call.respond(status = HttpStatusCode.OK, behandlinger)
//    }
}
