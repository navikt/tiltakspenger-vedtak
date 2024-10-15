package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import mu.KotlinLogging
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.toDTO
import no.nav.tiltakspenger.vedtak.routes.withMeldekortId
import no.nav.tiltakspenger.vedtak.routes.withSakId
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler

fun Route.hentMeldekortRoute(
    hentMeldekortService: HentMeldekortService,
    sakService: SakService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    val logger = KotlinLogging.logger { }
    get("/sak/{sakId}/meldekort") {
        logger.debug { "Mottatt get-request på /sak/{sakId}/meldekort - henter alle meldekort for sak" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withSakId { sakId ->
                val correlationId = call.correlationId()
                val meldekortperioder = hentMeldekortService.hentForSakId(
                    sakId = sakId,
                    saksbehandler = saksbehandler,
                    correlationId = correlationId,
                )
                val responseJsonPayload = meldekortperioder.toDTO()

                auditService.logMedSakId(
                    sakId = sakId,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.ACCESS,
                    contextMessage = "Henter alle meldekortene for en sak",
                    correlationId = correlationId,
                )

                call.respond(status = HttpStatusCode.OK, message = responseJsonPayload)
            }
        }
    }

    get("/sak/{sakId}/meldekort/{meldekortId}") {
        logger.debug { "Motatt get-request på /sak/{sakId}/meldekort/{meldekortId}" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withMeldekortId { meldekortId ->
                val correlationId = call.correlationId()
                // TODO post-mvp jah: Det skal holde å hente saken. Meldekortet kan hentes fra saken.
                val meldekort = hentMeldekortService.hentForMeldekortId(
                    meldekortId = meldekortId,
                    saksbehandler = saksbehandler,
                    correlationId = correlationId,
                )
                checkNotNull(meldekort) { "Meldekort med id $meldekortId eksisterer ikke i databasen" }
                val sak = sakService.hentForSakId(meldekort.sakId, saksbehandler, correlationId = correlationId)
                checkNotNull(sak) { "Sak med saksId ${meldekort.sakId} fra meldekort med iden $meldekortId finnes ikke." }

                auditService.logMedMeldekortId(
                    meldekortId = meldekortId,
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.ACCESS,
                    contextMessage = "Henter meldekort",
                    correlationId = correlationId,
                )
                // TODO pre-mvp: Her blir det mer riktig og bruke den totale perioden det skal meldes for.
                call.respond(status = HttpStatusCode.OK, message = meldekort.toDTO(sak.vedtaksperiode!!))
            }
        }
    }
}
