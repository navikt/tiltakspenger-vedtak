package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonService
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.kvp.KvpVilkårService
import no.nav.tiltakspenger.saksbehandling.service.behandling.vilkår.livsopphold.LivsoppholdVilkårService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
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

private val LOG = KotlinLogging.logger {}

internal const val BEHANDLING_PATH = "/behandling"
internal const val BEHANDLINGER_PATH = "/behandlinger"

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    personService: PersonService,
    kvpVilkårService: KvpVilkårService,
    livsoppholdVilkårService: LivsoppholdVilkårService,
    auditService: AuditService,
) {
    get("$BEHANDLING_PATH/{behandlingId}") {
        sikkerlogg.debug("Mottatt request på $BEHANDLING_PATH/behandlingId")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val behandling = behandlingService.hentBehandling(behandlingId, saksbehandler).toDTO()

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter hele behandlingen",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, behandling)
    }

    post("$BEHANDLING_PATH/beslutter/{behandlingId}") {
        sikkerlogg.debug("Mottatt request. $BEHANDLING_PATH/ skal sendes til beslutter")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler)

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Sender behandlingen til beslutter",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$BEHANDLING_PATH/avbrytbehandling/{behandlingId}") {
        sikkerlogg.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.frataBehandling(behandlingId, saksbehandler)

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Avbryter behandling",
            callId = call.callId,
        )

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    hentPersonRoute(innloggetSaksbehandlerProvider, sakService, personService, auditService)
    tiltakDeltagelseRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    institusjonsoppholdRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    kvpRoutes(innloggetSaksbehandlerProvider, kvpVilkårService, behandlingService, auditService)
    livsoppholdRoutes(innloggetSaksbehandlerProvider, livsoppholdVilkårService, behandlingService, auditService)
    introRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    alderRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    kravfristRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    stønadsdagerRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
}
