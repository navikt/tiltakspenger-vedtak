package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.periodisering.tilstøter
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import java.time.LocalDate

/**
 * @property beløp kan være 0 for dager som ikke gir utbetaling.
 */
data class Utbetalingsdag(
    val dato: LocalDate,
    val tiltakstype: TiltakstypeSomGirRett,
    val status: Status,
    val meldekortId: MeldekortId,
    val sats: Sats,
) {
    // Legg til barnetillegg etter MVP
    val beløp: Int = when (status) {
        Status.FullUtbetaling -> sats.sats
        Status.DelvisUtbetaling -> sats.satsDelvis
    }

    init {
        require(sats.periode.inneholder(dato)) { "Satsens periode må inneholde utbetalingsdagens dato, men var $dato" }
        require(beløp > 0) { "Beløp må være større enn 0, men var $beløp" }
    }

    enum class Status {
        FullUtbetaling,
        DelvisUtbetaling,
    }

    fun kanSlåSammen(annen: Meldekortdag): Boolean {
        return when (annen) {
            is Meldekortdag.Tiltaksdag ->
                this.tiltakstype == annen.tiltakstype &&
                    this.status == annen.status &&
                    this.tiltakstype == annen.tiltakstype &&
                    this.meldekortId == annen.meldekortId &&
                    this.dato.tilstøter(annen.dato)

            is Meldekortdag.IkkeTiltaksdag -> false
        }
    }
}

fun Meldekortdag.Tiltaksdag.genererUtbetalingsdag(): Utbetalingsdag {
    return Utbetalingsdag(
        dato = dato,
        tiltakstype = tiltakstype,
        status = status,
        meldekortId = meldekortId,
        sats = Satser.sats(dato),
    )
}
