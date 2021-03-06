package no.nav.tiltakspenger.domene

import com.google.common.collect.*
import java.time.LocalDate


//TODO: Needs more work
private class LocalDateDiscreteDomain : DiscreteDomain<LocalDate>() {
    override fun next(value: LocalDate): LocalDate {
        return value.plusDays(1)
    }

    override fun previous(value: LocalDate): LocalDate {
        return value.minusDays(1)
    }

    override fun distance(start: LocalDate, end: LocalDate): Long {
        return start.until(end).days.toLong()
        //return DAYS.between(start, end)
    }
}


class Periode(fra: LocalDate, til: LocalDate) {
    val range: Range<LocalDate> = Range.closed(fra, til)
    val fra: LocalDate
        get() = range.fraOgMed()
    val til: LocalDate
        get() = range.tilOgMed()

    fun inneholderHele(periode: Periode) = this.range.encloses(periode.range)

    fun overlapperMed(periode: Periode) = try {
        !this.range.intersection(periode.range).isEmpty
    } catch (iae: IllegalArgumentException) {
        false
    }

    fun overlappendePeriode(periode: Periode) = this.range.intersection(periode.range).toPeriode()

    fun ikkeOverlappendePeriode(periode: Periode): List<Periode> {
        val rangeSet: RangeSet<LocalDate> = TreeRangeSet.create()
        rangeSet.add(this.range)
        rangeSet.remove(periode.range)
        return rangeSet.asRanges().toPerioder()
    }

    fun ikkeOverlappendePerioder(perioder: List<Periode>): List<Periode> {
        val rangeSet: RangeSet<LocalDate> = TreeRangeSet.create()
        rangeSet.add(this.range)
        perioder.forEach { periode -> rangeSet.remove(periode.range) }
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

    fun inneholder(f??dselsdato: LocalDate): Boolean = range.contains(f??dselsdato)

    fun etter(f??dselsdato: LocalDate): Boolean = this.fra.isAfter(f??dselsdato)

    fun f??r(f??dselsdato: LocalDate): Boolean = this.til.isBefore(f??dselsdato)

    fun trekkFra(andrePerioder: List<Periode>): List<Periode> {
        val opprinneligeRangeSet =
            ImmutableRangeSet.Builder<LocalDate>().add(this.range).build()
        val andrePeriodeRangeSet =
            ImmutableRangeSet.Builder<LocalDate>().addAll(andrePerioder.map { it.range }).build()
        val ranges = opprinneligeRangeSet.difference(andrePeriodeRangeSet).asRanges()
        return ranges.filter { !it.canonical(LocalDateDiscreteDomain()).isEmpty }.map { it.toPeriode() }
    }

    fun tilDager() : List<LocalDate> {
        return fra.datesUntil(til.plusDays(1)).toList()
    }
}

fun Set<Range<LocalDate>>.toPerioder() = this.map { it.toPeriode() }
fun Range<LocalDate>.toPeriode(): Periode = Periode(this.fraOgMed(), this.tilOgMed())
fun Range<LocalDate>.fraOgMed(): LocalDate =
    if (this.lowerBoundType() == BoundType.CLOSED) this.lowerEndpoint() else this.lowerEndpoint().plusDays(1)

fun Range<LocalDate>.tilOgMed(): LocalDate =
    if (this.upperBoundType() == BoundType.CLOSED) this.upperEndpoint() else this.upperEndpoint().minusDays(1)