package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"

data class VilkårsvurderingBody(
    val saksbehandlerId: String,
    val vilkår: String,
    val behandlingsId: String

)
fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
) {
    get("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)
        val behandling = behandlingService.hentBehandling(behandlingId)
    }

    post("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/")

    }
}
