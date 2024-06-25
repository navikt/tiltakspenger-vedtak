package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingPath
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.hentKvpRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    kvpVilkårService: KvpVilkårService,
) {
    get("$behandlingPath/{behandlingId}/vilkår/kvp") {
        SECURELOG.debug("Mottatt request på $behandlingPath/{behandlingId}/vilkår/kvp")

        innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        kvpVilkårService.hent(behandlingId).let {
            call.respond(
                status = HttpStatusCode.OK,
                message = it.toDTO(),
            )
        }
    }
}
