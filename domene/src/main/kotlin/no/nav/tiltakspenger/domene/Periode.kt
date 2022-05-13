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

    fun intersect(periode: Periode): Periode {
        return Periode(
            fra = maxOf(periode.fra, this.fra),
            til = minOf(periode.til, this.til)
        )
    }
}
