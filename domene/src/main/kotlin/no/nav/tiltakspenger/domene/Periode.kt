package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Periode(val fra: LocalDate, val til: LocalDate) : ClosedRange<LocalDate> {
    override val endInclusive: LocalDate
        get() = til
    override val start: LocalDate
        get() = fra

    fun inneholderHele(periode: Periode): Boolean {
        return fra.isBefore(periode.fra) && til.isAfter(periode.til)
    }
    fun overlapperMed(periode: Periode): Boolean {
        return periode.contains(fra) || periode.contains(til)
    }
}
