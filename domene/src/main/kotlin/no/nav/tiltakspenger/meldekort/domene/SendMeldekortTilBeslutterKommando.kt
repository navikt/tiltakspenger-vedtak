package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDate

/**
 * Representerer en saksbehandler som fyller ut hele meldekortet, godkjenner og sender til beslutter.
 * Denne flyten vil bli annerledes for veileder og bruker.
 * Vi gjør ingen validering i denne klassen, det gjøres heller av [no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort]
 */
class SendMeldekortTilBeslutterKommando(
    val sakId: SakId,
    val meldekortId: MeldekortId,
    val saksbehandler: Saksbehandler,
    val dager: List<Dag>,
) {
    val periode: Periode = Periode(dager.first().dag, dager.last().dag)
    data class Dag(
        val dag: LocalDate,
        val status: Status,
    )

    enum class Status {
        /** Vi tar i mot SPERRET siden det er det saksbehandler ser/sender inn, men vi vil validere at dagen matcher med meldekortutkastet. */
        SPERRET,
        DELTATT_UTEN_LØNN_I_TILTAKET,
        DELTATT_MED_LØNN_I_TILTAKET,
        IKKE_DELTATT,
        FRAVÆR_SYK,
        FRAVÆR_SYKT_BARN,
        FRAVÆR_VELFERD_GODKJENT_AV_NAV,
        FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV,
    }
}
