package no.nav.tiltakspenger.vedtak.routes.sak

import arrow.core.Either
import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
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
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.respond400BadRequest
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler
import no.nav.tiltakspenger.vedtak.routes.withSaksnummer

private val logger = KotlinLogging.logger {}

internal const val SAK_PATH = "/sak"

fun Route.sakRoutes(
    sakService: SakService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    get("$SAK_PATH/{saksnummer}") {
        logger.debug { "Mottatt get-request på $SAK_PATH/{saksnummer}" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withSaksnummer { saksnummer ->
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
        logger.debug { "Mottatt post-request på $SAK_PATH" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            val fnr = Either.catch { Fnr.fromString(call.receive<FnrDTO>().fnr) }.getOrElse {
                call.respond400BadRequest(Standardfeil.fantIkkeFnr())
                return@withSaksbehandler
            }
            val correlationId = call.correlationId()

            sakService.hentForFnr(fnr, saksbehandler, correlationId).fold(
                ifLeft = {
                    call.respond400BadRequest(Standardfeil.fantIkkeFnr())
                },
                ifRight = {
                    auditService.logMedBrukerId(
                        brukerId = fnr,
                        navIdent = saksbehandler.navIdent,
                        action = AuditLogEvent.Action.ACCESS,
                        contextMessage = "Henter alle saker på brukeren",
                        correlationId = correlationId,
                    )
                    val sakDTO = it.toDTO()
                    call.respond(message = sakDTO, status = HttpStatusCode.OK)
                },
            )
        }
    }
}
