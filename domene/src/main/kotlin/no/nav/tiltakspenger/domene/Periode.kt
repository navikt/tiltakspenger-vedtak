package no.nav.tiltakspenger.domene

import com.google.common.collect.BoundType
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet
import java.time.LocalDate


class Periode(fra: LocalDate, til: LocalDate) {
    val range : Range<LocalDate> = Range.closed(fra, til)
    val fra: LocalDate
        get() = range.lowerEndpoint()
    val til: LocalDate
        get() = range.upperEndpoint()

    fun inneholderHele(periode: Periode) = this.range.encloses(periode.range)

    fun overlapperMed(periode: Periode) = try {
        !this.range.intersection(periode.range).isEmpty
    } catch( iae: IllegalArgumentException) {
        false
    }

    fun overlappendePeriode(periode: Periode) = this.range.intersection(periode.range).toPeriode()

    fun ikkeOverlappendePeriode(periode: Periode): List<Periode> {
        val rangeSet: RangeSet<LocalDate> = TreeRangeSet.create()
        rangeSet.add(this.range)
        rangeSet.remove(periode.range)
        return rangeSet.asRanges().toPerioder()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Periode) return false

        if (range != other.range) return false

        return true
    }

    override fun hashCode(): Int {
        return range.hashCode()
    }

    override fun toString(): String {
        return "Periode(range=$range)"
    }

}

fun Set<Range<LocalDate>>.toPerioder() = this.map { it.toPeriode() }

fun Range<LocalDate>.toPeriode(): Periode {
    val fra = if( this.lowerBoundType() == BoundType.CLOSED ) this.lowerEndpoint() else this.lowerEndpoint().plusDays(1)
    val til = if(this.upperBoundType() == BoundType.CLOSED) this.upperEndpoint() else this.upperEndpoint().minusDays(1)
    return Periode(fra, til)
}
