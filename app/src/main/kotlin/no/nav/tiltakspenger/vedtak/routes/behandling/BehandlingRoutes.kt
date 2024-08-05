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
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.mapSammenstillingDTO
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

internal const val behandlingPath = "/behandling"
internal const val behandlingerPath = "/behandlinger"

data class IdentDTO(
    val ident: String?,
)

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    attesteringRepo: AttesteringRepo,
    kvpVilkårService: KvpVilkårService,
    livsoppholdVilkårService: LivsoppholdVilkårService,
) {
    get("$behandlingPath/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val sak = sakService.hentMedBehandlingId(behandlingId, saksbehandler)

        if (sak.personopplysninger.erTom()) {
            return@get call.respond(
                message = "Sak mangler personopplysninger",
                status = HttpStatusCode.NotFound,
            )
        }

        val behandling = sak.behandlinger.filterIsInstance<Førstegangsbehandling>().firstOrNull {
            it.id == behandlingId
        } ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        // her burde vi nok ikke bare hente den første, men finne den riktige og evnt feilmelding hvis vi ikke finner den
        // val behandling = behandlingService.hentBehandling(behandlingId) Skal vi hente behandling direkte eller via sak?

        val attesteringer = attesteringRepo.hentForBehandling(behandling.id)

        val dto = mapSammenstillingDTO(
            behandling = behandling,
            personopplysninger = sak.personopplysninger.søkere(),
            attesteringer = attesteringer,
        )
        call.respond(status = HttpStatusCode.OK, dto)
    }

    post("$behandlingPath/beslutter/{behandlingId}") {
        SECURELOG.debug("Mottatt request. $behandlingPath/ skal sendes til beslutter")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/avbrytbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.frataBehandling(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    tiltakDeltagelseRoutes(innloggetSaksbehandlerProvider, behandlingService)
    institusjonsoppholdRoutes(innloggetSaksbehandlerProvider, behandlingService)
    kvpRoutes(innloggetSaksbehandlerProvider, kvpVilkårService, behandlingService)
    livsoppholdRoutes(innloggetSaksbehandlerProvider, livsoppholdVilkårService, behandlingService)
    introRoutes(innloggetSaksbehandlerProvider, behandlingService)
    alderRoutes(innloggetSaksbehandlerProvider, behandlingService)
    kravfristRoutes(innloggetSaksbehandlerProvider, behandlingService)
}
