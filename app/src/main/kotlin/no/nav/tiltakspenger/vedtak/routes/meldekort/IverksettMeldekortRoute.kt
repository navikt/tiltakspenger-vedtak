package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.service.AuditLogEvent
import no.nav.tiltakspenger.felles.service.AuditService
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.toDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

fun Route.iverksettMeldekortRoute(
    iverksettMeldekortService: IverksettMeldekortService,
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    auditService: AuditService,
) {
    val logger = KotlinLogging.logger { }
    post("sak/{sakId}/meldekort/{meldekortId}/iverksett") {
        logger.info { "Mottatt request på $MELDEKORT_PATH/{meldekortId}/iverksett" }
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val meldekortId =
            call.parameters["meldekortId"]?.let { MeldekortId.fromString(it) }
                ?: return@post call.respond(message = "meldekortId mangler", status = HttpStatusCode.NotFound)

        val sakId =
            call.parameters["sakId"]?.let { SakId.fromString(it) }
                ?: return@post call.respond(message = "sakId mangler", status = HttpStatusCode.NotFound)
        logger.info { "Meldekort med id $meldekortId skal godkjennes" }
        val meldekort = iverksettMeldekortService.iverksettMeldekort(
            IverksettMeldekortKommando(
                meldekortId = meldekortId,
                beslutter = saksbehandler,
                sakId = sakId,
            ),
        )

        auditService.logMedMeldekortId(
            meldekortId = meldekortId,
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.UPDATE,
            callId = call.callId,
        )

        call.respond(message = meldekort.toDTO(), status = HttpStatusCode.OK)
    }
}
