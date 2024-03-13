package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    søkerService: SøkerService,
) {
    get(behandlingerPath) {
        SECURELOG.debug("Mottatt request på $behandlingerPath")

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val behandlinger = behandlingService.hentAlleBehandlinger(saksbehandler)
            .mapBehandlinger()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/startbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.taBehandling(behandlingId, saksbehandler) // Bør kanskje sjekke rolle dypere

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    get("$behandlingerPath/hentForIdent/{søkerId}") {
        SECURELOG.debug { "Mottatt request om å hente alle behandlinger for en ident" }
        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val søkerId = call.parameters["søkerId"]?.let { SøkerId.fromDb(it) }
            ?: return@get call.respond(message = "SøkerId ikke funnet", status = HttpStatusCode.NotFound)

        val ident = søkerService.hentIdent(søkerId, saksbehandler)
            ?: return@get call.respond(message = "Fant ikke ident for søker", status = HttpStatusCode.NotFound)

        val behandlinger = behandlingService.hentBehandlingForIdent(ident, saksbehandler)
            .mapBehandlinger()

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }
}

fun List<Førstegangsbehandling>.mapBehandlinger(): List<BehandlingDTO> =
    this.map {
        BehandlingDTO(
            id = it.id.toString(),
            ident = it.søknad().personopplysninger.ident,
            saksbehandler = it.saksbehandler,
            beslutter = when (it) {
                is BehandlingIverksatt -> it.beslutter
                is BehandlingTilBeslutter -> it.beslutter
                else -> null
            },
            status = finnStatus(it),
            typeBehandling = "Førstegangsbehandling",
            fom = it.vurderingsperiode.fra,
            tom = it.vurderingsperiode.til,
        )
    }.sortedBy { it.id }
