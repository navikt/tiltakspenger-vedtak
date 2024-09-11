package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær.Reduksjon
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort
import no.nav.tiltakspenger.utbetaling.domene.Satser
import java.time.LocalDate

data class Beregningsdag(
    val beløp: Int,
    val prosent: Int,
    val satsdag: Satsdag,
    val dato: LocalDate,
) {
    init {
        require(dato == satsdag.dato)
    }
}
fun beregnDag(dato: LocalDate, reduksjon: ReduksjonAvYtelsePåGrunnAvFravær): Beregningsdag = Satser.sats(dato).let {
    val prosent = when (reduksjon) {
        IngenReduksjon -> 100
        Reduksjon -> 75
        YtelsenFallerBort -> 0
    }
    Beregningsdag(
        beløp = when (reduksjon) {
            IngenReduksjon -> it.sats
            Reduksjon -> it.satsRedusert
            YtelsenFallerBort -> 0
        },
        prosent = prosent,
        satsdag = it,
        dato = dato,
    )
}
