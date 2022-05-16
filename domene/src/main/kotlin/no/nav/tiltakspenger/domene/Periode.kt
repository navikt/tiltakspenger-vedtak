package no.nav.tiltakspenger.domene

import java.time.LocalDate

data class Periode(val fra: LocalDate, val til: LocalDate) : ClosedRange<LocalDate> {
    override val endInclusive: LocalDate
        get() = til
    override val start: LocalDate
        get() = fra


    fun inneholderHele(periode: Periode): Boolean {
        return fra <= periode.fra && til >= periode.til
    }

    fun overlapperMed(periode: Periode): Boolean {
        return periode.contains(fra) || periode.contains(til)
    }

    fun overlappendePeriode(periode: Periode): Periode {
        return Periode(
            fra = maxOf(periode.fra, this.fra),
            til = minOf(periode.til, this.til)
        )
    }

    fun ikkeOverlappendePeriode(periode: Periode): List<Periode> {
        return listOf(
            Periode(
                fra = minOf(this.fra, periode.fra),
                til = maxOf(this.fra, periode.fra)
            ).overlappendePeriode(this),
            Periode(
                fra = minOf(this.til, periode.til),
                til = maxOf(this.til, periode.til)
            ).overlappendePeriode(this),
        ).filterNot { it.isEmpty() }
    }
}
