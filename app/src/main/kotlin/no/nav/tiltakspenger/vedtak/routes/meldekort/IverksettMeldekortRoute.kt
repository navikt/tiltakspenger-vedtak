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
import no.nav.tiltakspenger.meldekort.domene.IverksettMeldekortKommando
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import java.util.UUID

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
            call.parameters["meldekortId"]?.let { UUID.fromString(it) }
                ?: return@post call.respond(message = "meldekortId mangler", status = HttpStatusCode.NotFound)

        logger.info { "Meldekort med id $meldekortId skal godkjennes" }
        val meldekort =
            iverksettMeldekortService.iverksettMeldekort(
                IverksettMeldekortKommando(
                    meldekortId = MeldekortId.fromString(meldekortId),
                    beslutter = saksbehandler,
                ),
            )

        auditService.logMedMeldekortId(
            meldekortId = MeldekortId.Companion.fromString(meldekortId),
            navIdent = saksbehandler.navIdent,
            action = AuditLogEvent.Action.CREATE,
            callId = call.callId,
        )

        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}
