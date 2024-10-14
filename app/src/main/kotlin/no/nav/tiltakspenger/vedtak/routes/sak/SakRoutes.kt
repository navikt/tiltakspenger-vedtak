package no.nav.tiltakspenger.vedtak.routes.sak

import arrow.core.Either
import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val SAK_PATH = "/sak"

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
            correlationId = call.correlationId(),
        )
        val sakDTO = sakService.hentForSaksnummer(saksnummer, saksbehandler, correlationId = call.correlationId()).toDTO()
        call.respond(message = sakDTO, status = HttpStatusCode.OK)
    }

    post(SAK_PATH) {
        LOG.debug("Mottatt request på $SAK_PATH")
        val fnr = Either.catch { Fnr.fromString(call.receive<FnrDTO>().fnr) }.getOrElse {
            call.respond(message = "Fødselsnummeret er ugyldig", status = HttpStatusCode.BadRequest)
            LOG.error { "Klarte ikke deserialisere respons fra pdl. Se sikkerlog for mer informasjon" }
            sikkerlogg.error(it) { "Klarte ikke deserialisere respons fra pdl. respons: $this " }
            throw it
        }
        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)

        sakService.hentForFnr(fnr, saksbehandler, correlationId = call.correlationId()).fold(
            ifLeft = {
                call.respond(
                    message = "Fant ikke sak på fødselsnummer",
                    status = HttpStatusCode.BadRequest,
                )
            },
            ifRight = {
                auditService.logMedBrukerId(
                    brukerId = fnr,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.ACCESS,
                    contextMessage = "Henter alle saker på brukeren",
                    correlationId = call.correlationId(),
                )
                val sakDTO = it.toDTO()
                call.respond(message = sakDTO, status = HttpStatusCode.OK)
            },
        )
    }
}
