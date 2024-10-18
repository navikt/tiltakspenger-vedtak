package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
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
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger.hentPersonRoute
import no.nav.tiltakspenger.vedtak.routes.behandling.stønadsdager.stønadsdagerRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder.alderRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold.institusjonsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet.introRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravfrist.kravfristRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.kvpRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.livsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltakdeltagelse.tiltakDeltagelseRoutes
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

internal const val BEHANDLING_PATH = "/behandling"
internal const val BEHANDLINGER_PATH = "/behandlinger"

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    tokenService: TokenService,
    sakService: SakService,
    kvpVilkårService: KvpVilkårService,
    livsoppholdVilkårService: LivsoppholdVilkårService,
    auditService: AuditService,
) {
    val logger = KotlinLogging.logger {}
    get("$BEHANDLING_PATH/{behandlingId}") {
        logger.debug("Mottatt get-request på '$BEHANDLING_PATH/{behandlingId}' - henter hele behandlingen")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val correlationId = call.correlationId()
        val behandling = behandlingService.hentBehandling(behandlingId, saksbehandler, correlationId).toDTO()

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter hele behandlingen",
            correlationId = correlationId,
        )

        call.respond(status = HttpStatusCode.OK, behandling)
    }

    post("$BEHANDLING_PATH/beslutter/{behandlingId}") {
        logger.debug("Mottatt post-request på '$BEHANDLING_PATH/beslutter/{behandlingId}' - sender behandling til beslutter")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler, correlationId = call.correlationId())

        auditService.logMedBehandlingId(
            behandlingId = behandlingId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            contextMessage = "Sender behandlingen til beslutter",
            correlationId = call.correlationId(),
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    hentPersonRoute(tokenService, sakService, auditService)
    tiltakDeltagelseRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    institusjonsoppholdRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    kvpRoutes(innloggetSaksbehandlerProvider, kvpVilkårService, behandlingService, auditService)
    livsoppholdRoutes(innloggetSaksbehandlerProvider, livsoppholdVilkårService, behandlingService, auditService)
    introRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    alderRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    kravfristRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
    stønadsdagerRoutes(innloggetSaksbehandlerProvider, behandlingService, auditService)
}
