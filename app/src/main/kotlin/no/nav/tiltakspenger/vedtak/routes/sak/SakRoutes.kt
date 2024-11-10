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
import no.nav.tiltakspenger.libs.auth.core.TokenService
import no.nav.tiltakspenger.libs.auth.ktor.withSaksbehandler
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.service.sak.KunneIkkeHenteSakForFnr
import no.nav.tiltakspenger.saksbehandling.service.sak.KunneIkkeHenteSakForSaksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.Standardfeil.ikkeTilgang
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.respond400BadRequest
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.respond403Forbidden
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.respond404NotFound
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
                sakService.hentForSaksnummer(
                    saksnummer = saksnummer,
                    saksbehandler = saksbehandler,
                    correlationId = call.correlationId(),
                ).fold(
                    {
                        when (it) {
                            is KunneIkkeHenteSakForSaksnummer.HarIkkeTilgang -> call.respond403Forbidden(ikkeTilgang("Må ha en av rollene ${it.kreverEnAvRollene} for å hente sak for saksnummer."))
                        }
                    },
                    { sak ->
                        call.respond(message = sak.toDTO(), status = HttpStatusCode.OK)
                    },
                )
            }
        }
    }

    post(SAK_PATH) {
        logger.debug { "Mottatt post-request på $SAK_PATH" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            val fnr = Either.catch { Fnr.fromString(call.receive<FnrDTO>().fnr) }.getOrElse {
                call.respond400BadRequest(
                    melding = "Forventer at fødselsnummeret er 11 siffer",
                    kode = "ugyldig_fnr",
                )
                return@withSaksbehandler
            }
            val correlationId = call.correlationId()

            sakService.hentForFnr(fnr, saksbehandler, correlationId).fold(
                ifLeft = {
                    when (it) {
                        is KunneIkkeHenteSakForFnr.FantIkkeSakForFnr -> call.respond404NotFound(Standardfeil.fantIkkeFnr())
                        is KunneIkkeHenteSakForFnr.HarIkkeTilgang -> call.respond403Forbidden(ikkeTilgang("Må ha en av rollene ${it.kreverEnAvRollene} for å hente sak for fnr."))
                    }
                },
                ifRight = {
                    auditService.logMedBrukerId(
                        brukerId = fnr,
                        navIdent = saksbehandler.navIdent,
                        action = AuditLogEvent.Action.SEARCH,
                        contextMessage = "Søker opp alle sakene til brukeren",
                        correlationId = correlationId,
                    )
                    val sakDTO = it.toDTO()
                    call.respond(message = sakDTO, status = HttpStatusCode.OK)
                },
            )
        }
    }
}
