package no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.routes.sak.SAK_PATH
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.hentPersonRoute(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    sakService: SakService,
    personService: PersonService,
    auditService: AuditService,
) {
    get("$SAK_PATH/{sakId}/personopplysninger") {
        sikkerlogg.debug("Mottatt request på $SAK_PATH/{sakId}/personopplysninger")
        Either.catch {
            val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
            val sakId = SakId.fromString(call.parameter("sakId"))

            val fnr = sakService.hentFnrForSakId(sakId)

            require(fnr != null) { "Fant ikke fødselsnummer på sak med sakId: $sakId" }

            val personopplysninger = personService.hentEnkelPersonForFnr(fnr).toDTO(skjerming = false)

            auditService.logMedSakId(
                sakId = sakId,
                navIdent = saksbehandler.navIdent,
                action = AuditLogEvent.Action.ACCESS,
                contextMessage = "Henter personopplysninger for en behandling",
                correlationId = call.correlationId(),
            )

            call.respond(status = HttpStatusCode.OK, personopplysninger)
        }.onLeft {
            sikkerlogg.error(it) { "Henting av personopplysninger feilet: ${it.message}" }
            throw it
        }
    }
}
