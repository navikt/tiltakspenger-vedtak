package no.nav.tiltakspenger.vedtak.routes.sak

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.Standardfeil
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.respond400BadRequest
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler
import no.nav.tiltakspenger.vedtak.routes.withSaksnummer

private val LOG = KotlinLogging.logger {}

internal const val SAK_PATH = "/sak"

fun Route.sakRoutes(
    sakService: SakService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    get("$SAK_PATH/{saksnummer}") {
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withSaksnummer { saksnummer ->
                LOG.debug("Mottatt request på $SAK_PATH/{saksnummer}")

                auditService.logMedSaksnummer(
                    saksnummer = saksnummer,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.ACCESS,
                    contextMessage = "Henter hele saken til brukeren",
                    correlationId = call.correlationId(),
                )
                val sakDTO = sakService.hentForSaksnummer(
                    saksnummer = saksnummer,
                    saksbehandler = saksbehandler,
                    correlationId = call.correlationId(),
                ).toDTO()
                call.respond(message = sakDTO, status = HttpStatusCode.OK)
            }
        }
    }

    post(SAK_PATH) {
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            LOG.debug("Mottatt request på $SAK_PATH")
            val fnr = Fnr.fromString(call.receive<FnrDTO>().fnr)
            val correlationId = call.correlationId()

            auditService.logMedBrukerId(
                brukerId = fnr,
                navIdent = saksbehandler.navIdent,
                action = AuditLogEvent.Action.ACCESS,
                contextMessage = "Henter alle saker på brukeren",
                correlationId = correlationId,
            )

            sakService.hentForFnr(fnr, saksbehandler, correlationId).fold(
                ifLeft = { call.respond400BadRequest(Standardfeil.fantIkkeFnr()) },
                ifRight = {
                    val sakDTO = it.toDTO()
                    call.respond(message = sakDTO, status = HttpStatusCode.OK)
                },
            )
        }
    }
}
