package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.routes.behandling.SaksopplysningDTO.Companion.lagSaksopplysningMedVilkår
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"
internal const val behandlingerPath = "/behandlinger"

data class BehandlingDTO(
    val id: String,
    val ident: String,
)

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
) {
    get("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)
        val sak = sakService.henteMedBehandlingsId(behandlingId) ?: return@get call.respond(
            message = "Sak ikke funnet",
            status = HttpStatusCode.NotFound,
        )

        val behandling = sak.behandlinger.filterIsInstance<Søknadsbehandling>().firstOrNull {
            it.id == behandlingId
        } ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        // her burde vi nok ikke bare hente den første, men finne den riktige og evnt feilmelding hvis vi ikke finner den
        // val behandling = behandlingService.hentBehandling(behandlingId) Skal vi hente behandling direkte eller via sak?

        val dto = mapSammenstillingDTO(
            behandling = behandling,
            personopplysninger = sak.personopplysninger,
        )
        call.respond(status = HttpStatusCode.OK, dto)
    }

    get(behandlingerPath) {
        LOG.debug("Mottatt request på $behandlingerPath")
        val behandlinger = behandlingService.hentAlleBehandlinger().map {
            BehandlingDTO(id = it.id.toString(), ident = it.søknad().personopplysninger.ident)
        }

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val nySaksopplysning = call.receive<SaksopplysningDTO>()
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.leggTilSaksopplysning(
            behandlingId,
            lagSaksopplysningMedVilkår(saksbehandler.navIdent, nySaksopplysning),
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }
}
