package no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.hentPersonRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    sakService: SakService,
    personGateway: PersonGateway,
    auditService: AuditService,
) {
    get("$BEHANDLING_PATH/{sakId}/personopplysninger") {
        sikkerlogg.debug("Mottatt request på $BEHANDLING_PATH/{sakId}/personopplysninger")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val sakId = SakId.fromString(call.parameter("sakId"))

        val fnr = sakService.hentFnrForSakId(sakId)

        require(fnr != null) { "Fant ikke fødselsnummer på sak med sakId: $sakId" }

        val personopplysninger = runBlocking { personGateway.hentEnkelPerson(fnr) }.toDTO()

        auditService.logMedSakId(
            sakId = sakId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter personopplysninger for en behandling",
            callId = call.callId,
        )

        call.respond(status = HttpStatusCode.OK, personopplysninger)
    }
}
