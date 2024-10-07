package no.nav.tiltakspenger.vedtak.routes.meldekort

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.domene.KanIkkeSendeMeldekortTilBeslutter
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Dag
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import java.time.LocalDate

private data class Body(
    val dager: List<Dag>,
) {
    data class Dag(
        val dato: String,
        val status: String,
    )

    fun toDomain(
        saksbehandler: Saksbehandler,
        meldekortId: MeldekortId,
        sakId: SakId,
        correlationId: CorrelationId,
    ) = SendMeldekortTilBeslutterKommando(
        sakId = sakId,
        saksbehandler = saksbehandler,
        correlationId = correlationId,
        dager =
        this.dager.map { dag ->
            Dag(
                dag = LocalDate.parse(dag.dato),
                status =
                when (dag.status) {
                    "SPERRET" -> SendMeldekortTilBeslutterKommando.Status.SPERRET
                    "DELTATT_UTEN_LØNN_I_TILTAKET" -> SendMeldekortTilBeslutterKommando.Status.DELTATT_UTEN_LØNN_I_TILTAKET
                    "DELTATT_MED_LØNN_I_TILTAKET" -> SendMeldekortTilBeslutterKommando.Status.DELTATT_MED_LØNN_I_TILTAKET
                    "IKKE_DELTATT" -> SendMeldekortTilBeslutterKommando.Status.IKKE_DELTATT
                    "FRAVÆR_SYK" -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYK
                    "FRAVÆR_SYKT_BARN" -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYKT_BARN
                    "FRAVÆR_VELFERD_GODKJENT_AV_NAV" -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_VELFERD_GODKJENT_AV_NAV
                    "FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV" -> SendMeldekortTilBeslutterKommando.Status.FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
                    else -> throw IllegalArgumentException("Ukjent status: ${dag.status}")
                },
            )
        },
        meldekortId = meldekortId,
    )
}

fun Route.sendMeldekortTilBeslutterRoute(
    sendMeldekortTilBeslutterService: SendMeldekortTilBeslutterService,
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    auditService: AuditService,
) {
    post("/sak/{sakId}/meldekort/{meldekortId}") {
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val meldekortId =
            call.parameters["meldekortId"]
                ?: return@post call.respond(message = "meldekortId mangler", status = HttpStatusCode.NotFound)
        val sakId = call.parameters["sakId"]
            ?: return@post call.respond(message = "sakId mangler", status = HttpStatusCode.NotFound)
        val dto = call.receive<Body>()
        val meldekort =
            sendMeldekortTilBeslutterService.sendMeldekortTilBeslutter(
                dto.toDomain(
                    saksbehandler = saksbehandler,
                    meldekortId = MeldekortId.fromString(meldekortId),
                    sakId = SakId.fromString(sakId),
                    correlationId = call.correlationId(),
                ),
            )
        meldekort.fold(
            ifLeft = {
                call.respond(
                    message = when (it) {
                        is KanIkkeSendeMeldekortTilBeslutter.MeldekortperiodenKanIkkeVæreFremITid -> "Kan ikke sende inn et meldekort før meldekortperioden har begynt."
                        is KanIkkeSendeMeldekortTilBeslutter.MåVæreSaksbehandler -> "Mangler saksbehandler rolle."
                    },
                    status = HttpStatusCode.BadRequest,
                )
            },
            ifRight = {
                auditService.logMedMeldekortId(
                    meldekortId = MeldekortId.fromString(meldekortId),
                    navIdent = saksbehandler.navIdent,
                    action = AuditLogEvent.Action.UPDATE,
                    contextMessage = "Oppdaterer meldekort og sender til beslutter",
                    correlationId = call.correlationId(),
                )

                call.respond(message = {}, status = HttpStatusCode.OK)
            },
        )
    }
}
