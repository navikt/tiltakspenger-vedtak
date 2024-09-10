package no.nav.tiltakspenger.vedtak.routes.sak

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.service.AuditLogEvent
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val SAK_PATH = "/sak"

fun Sak.toDTO() =
    SakDTO(
        saksnummer = this.saksnummer.verdi,
        ident = this.fnr.verdi,
    )

fun Route.sakRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    sakService: SakService,
    auditService: AuditService,
) {
    get("$SAK_PATH/{saksnummer}") {
        LOG.debug("Mottatt request på $SAK_PATH/{saksnummer}")
        val saksnummer = Saksnummer(call.parameter("saksnummer"))
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        auditService.logMedSaksnummer(
            saksnummer = saksnummer,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.ACCESS,
            contextMessage = "Henter hele saken til brukeren",
            callId = call.callId,
        )
        val sakDTO = sakService.hentForSaksnummer(saksnummer, saksbehandler).toDTO()
        call.respond(message = sakDTO, status = HttpStatusCode.OK)
    }
}
