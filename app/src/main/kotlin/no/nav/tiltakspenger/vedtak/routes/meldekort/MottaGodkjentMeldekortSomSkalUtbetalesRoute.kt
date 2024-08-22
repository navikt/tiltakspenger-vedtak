package no.nav.tiltakspenger.vedtak.routes.meldekort

import arrow.core.toNonEmptyListOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortperiode
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.service.MottaUtfyltMeldekortService
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsdag
import no.nav.tiltakspenger.vedtak.routes.utbetaling.UtbetalingDagStatusDTO
import java.time.LocalDate

private val log = KotlinLogging.logger {}

private data class Body(
    val meldekortId: String,
    val sakId: String,
    val rammevedtakId: String,
    val dager: List<DagDTO>,
    val saksbehandler: String,
    val beslutter: String,
) {
    data class DagDTO(
        val dato: String,
        val tiltakstype: String,
        val status: String,
    )

    fun toDomain() =
        UtfyltMeldekort(
            id = MeldekortId.fromString(meldekortId),
            sakId = SakId.fromString(sakId),
            rammevedtakId = VedtakId.fromString(rammevedtakId),
            saksbehandler = saksbehandler,
            beslutter = beslutter,
            meldekortperiode =
            Meldekortperiode(
                verdi =
                dager
                    .map { dag ->
                        when (UtbetalingDagStatusDTO.valueOf(dag.status)) {
                            // TODO pre-mvp jah: Etter vi flytter meldekort til vedtak, må vi endre på dette.
                            UtbetalingDagStatusDTO.IngenUtbetaling ->
                                Meldekortdag.IkkeTiltaksdag(
                                    dato = LocalDate.parse(dag.dato),
                                    meldekortId = MeldekortId.Companion.fromString(meldekortId),
                                )

                            UtbetalingDagStatusDTO.FullUtbetaling,
                            UtbetalingDagStatusDTO.DelvisUtbetaling,
                            ->
                                Meldekortdag.Tiltaksdag(
                                    status = Utbetalingsdag.Status.valueOf(dag.status),
                                    dato = LocalDate.parse(dag.dato),
                                    tiltakstype = TiltakstypeSomGirRett.valueOf(dag.tiltakstype),
                                    meldekortId = MeldekortId.Companion.fromString(meldekortId),
                                )
                        }
                    }.toNonEmptyListOrNull()!!,
            ),
        )
}

/**
 * Når meldekort-api mottar et utfylt meldekort, enten fra bruker eller annen aktør, så sendes det tilbake til tiltakspenger-vedtak for godkjenning og utbetaling.
 */
fun Route.mottaUtfyltMeldekortRoute(service: MottaUtfyltMeldekortService) {
    post("meldekort/motta") {
        log.info { "Mottok utfylt meldekort fra meldekort-api" }
        val body = call.receive<Body>()
        log.debug { "Deserialiserte utfylt meldekort fra meldekort-api. SakId: ${body.sakId}, meldekortId: ${body.meldekortId}" }
        val vedtak = service.motta(body.toDomain())

        call.respond(status = HttpStatusCode.Accepted, message = "Utfylt meldekort mottatt og persistert.")
    }
}
