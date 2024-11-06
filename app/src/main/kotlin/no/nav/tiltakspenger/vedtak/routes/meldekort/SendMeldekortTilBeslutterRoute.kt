package no.nav.tiltakspenger.vedtak.routes.meldekort

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.UgyldigKontornummer
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.domene.KanIkkeSendeMeldekortTilBeslutter
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Dager
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.vedtak.auditlog.AuditLogEvent
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.routes.correlationId
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.respond400BadRequest
import no.nav.tiltakspenger.vedtak.routes.withBody
import no.nav.tiltakspenger.vedtak.routes.withMeldekortId
import no.nav.tiltakspenger.vedtak.routes.withSakId
import no.nav.tiltakspenger.vedtak.routes.withSaksbehandler
import java.time.LocalDate

private data class Body(
    val dager: List<Dag>,
    val navkontor: String,
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
    ): Either<UgyldigKontornummer, SendMeldekortTilBeslutterKommando> {
        return SendMeldekortTilBeslutterKommando(
            sakId = sakId,
            saksbehandler = saksbehandler,
            correlationId = correlationId,
            navkontor = Navkontor.tryCreate(navkontor).getOrElse { return it.left() },
            dager = Dager(
                this.dager.map { dag ->
                    Dager.Dag(
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
            ),
            meldekortId = meldekortId,
        ).right()
    }
}

fun Route.sendMeldekortTilBeslutterRoute(
    sendMeldekortTilBeslutterService: SendMeldekortTilBeslutterService,
    auditService: AuditService,
    tokenService: TokenService,
) {
    val logger = KotlinLogging.logger { }
    post("/sak/{sakId}/meldekort/{meldekortId}") {
        logger.debug { "Mottatt post-request på /sak/{sakId}/meldekort/{meldekortId} - saksbehandler har fylt ut meldekortet og sendt til beslutter" }
        call.withSaksbehandler(tokenService = tokenService) { saksbehandler ->
            call.withSakId { sakId ->
                call.withMeldekortId { meldekortId ->
                    call.withBody<Body> { body ->
                        val correlationId = call.correlationId()
                        val kommando = body.toDomain(
                            saksbehandler = saksbehandler,
                            meldekortId = meldekortId,
                            sakId = sakId,
                            correlationId = correlationId,
                        ).getOrElse {
                            call.respond400BadRequest(
                                melding = "Navkontor (enhetsnummer/kontornummer) er forventet å være 4 siffer.",
                                kode = "ugyldig_kontornummer",
                            )
                            return@withBody
                        }
                        sendMeldekortTilBeslutterService.sendMeldekortTilBeslutter(kommando).fold(
                            ifLeft = {
                                when (it) {
                                    is KanIkkeSendeMeldekortTilBeslutter.MeldekortperiodenKanIkkeVæreFremITid -> {
                                        call.respond400BadRequest(
                                            melding = "Kan ikke sende inn et meldekort før meldekortperioden har begynt.",
                                            kode = "meldekortperioden_kan_ikke_være_frem_i_tid",
                                        )
                                    }

                                    is KanIkkeSendeMeldekortTilBeslutter.MåVæreSaksbehandler -> {
                                        call.respond400BadRequest(
                                            melding = "Kan ikke sende meldekort til beslutter. Krever saksbehandler-rolle.",
                                            kode = "må_være_saksbehandler",
                                        )
                                    }

                                    is KanIkkeSendeMeldekortTilBeslutter.ForMangeDagerUtfylt -> {
                                        call.respond400BadRequest(
                                            melding = "Kan ikke sende meldekort til beslutter. For mange dager er utfylt. Maks antall for dette meldekortet er ${it.antallDagerForMeldeperiode}, mens antall utfylte dager er ${it.antallDagerUtfylt}.",
                                            kode = "for_mange_dager_utfylt",
                                        )
                                    }
                                }
                            },
                            ifRight = {
                                auditService.logMedMeldekortId(
                                    meldekortId = meldekortId,
                                    navIdent = saksbehandler.navIdent,
                                    action = AuditLogEvent.Action.UPDATE,
                                    contextMessage = "Saksbehandler har fylt ut meldekortet og sendt til beslutter",
                                    correlationId = correlationId,
                                )
                                call.respond(message = {}, status = HttpStatusCode.OK)
                            },
                        )
                    }
                }
            }
        }
    }
}
