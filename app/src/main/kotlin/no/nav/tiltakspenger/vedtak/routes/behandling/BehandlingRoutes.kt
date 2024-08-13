package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger.hentPersonRoute
import no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager.stønadsdagerRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.alderRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.institusjonsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.introRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.kravfristRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.kvpRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.livsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse.tiltakDeltagelseRoutes
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")
private val LOG = KotlinLogging.logger {}

internal const val BEHANDLING_PATH = "/behandling"
internal const val BEHANDLINGER_PATH = "/behandlinger"

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    kvpVilkårService: KvpVilkårService,
    livsoppholdVilkårService: LivsoppholdVilkårService,
) {
    get("$BEHANDLING_PATH/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $BEHANDLING_PATH/behandlingId")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val behandling = behandlingService.hentBehandling(behandlingId, saksbehandler).toDTO()

        call.respond(status = HttpStatusCode.OK, behandling)
    }

    post("$BEHANDLING_PATH/beslutter/{behandlingId}") {
        SECURELOG.debug("Mottatt request. $BEHANDLING_PATH/ skal sendes til beslutter")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$BEHANDLING_PATH/avbrytbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.frataBehandling(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    hentPersonRoute(innloggetSaksbehandlerProvider, sakService)
    tiltakDeltagelseRoutes(innloggetSaksbehandlerProvider, behandlingService)
    institusjonsoppholdRoutes(innloggetSaksbehandlerProvider, behandlingService)
    kvpRoutes(innloggetSaksbehandlerProvider, kvpVilkårService, behandlingService)
    livsoppholdRoutes(innloggetSaksbehandlerProvider, livsoppholdVilkårService, behandlingService)
    introRoutes(innloggetSaksbehandlerProvider, behandlingService)
    alderRoutes(innloggetSaksbehandlerProvider, behandlingService)
    kravfristRoutes(innloggetSaksbehandlerProvider, behandlingService)
    stønadsdagerRoutes(innloggetSaksbehandlerProvider, behandlingService)
}
