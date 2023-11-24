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
import no.nav.tiltakspenger.domene.behandling.harTilgang
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.personopplysning.PersonopplysningService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.behandlingBenkRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    personopplysningService: PersonopplysningService,
) {
    get(behandlingerPath) {
        SECURELOG.debug("Mottatt request på $behandlingerPath")

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val behandlinger = behandlingService.hentAlleBehandlinger()
            .filter { behandling -> personopplysningService.hent(behandling.sakId).harTilgang(saksbehandler) }
            .map {
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
                )
            }.sortedBy { it.id }

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/startbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER) || saksbehandler.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være saksbehandler eller beslutter" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.startBehandling(behandlingId, saksbehandler.navIdent) // Bør kanskje sjekke rolle dypere

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
