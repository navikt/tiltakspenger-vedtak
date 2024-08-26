package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær
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
    val beløp: Int =
        when (status) {
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

    /**
     * Kan slå sammen en utbetalingsdag med en meldekortdag dersom de har samme tiltakstype, meldekortId,  og er påfølgende dager.
     *
     * Merk at venste side må være dagen før høyre side for at de skal kunne slås sammen.
     */
    fun kanSlåSammen(neste: Meldekortdag.Utfylt): Boolean =
        this::class == neste::class &&
            this.tiltakstype == neste.tiltakstype &&
            this.meldekortId == neste.meldekortId &&
            this.dato.plusDays(1) == neste.dato &&
            this.status == neste.tilUtbetalingsstatus()
}

fun Meldekortdag.Utfylt.tilUtbetalingsstatus(): Utbetalingsdag.Status? {
    return when (this.reduksjon) {
        ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon -> Utbetalingsdag.Status.FullUtbetaling
        ReduksjonAvYtelsePåGrunnAvFravær.DelvisReduksjon -> Utbetalingsdag.Status.DelvisUtbetaling
        ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort -> null
    }
}

fun Meldekortdag.Utfylt.genererUtbetalingsdag(): Utbetalingsdag =
    Utbetalingsdag(
        dato = dato,
        tiltakstype = tiltakstype,
        status = tilUtbetalingsstatus()!!,
        meldekortId = meldekortId,
        sats = Satser.sats(dato),
    )
