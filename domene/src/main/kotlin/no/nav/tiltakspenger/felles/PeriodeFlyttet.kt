package no.nav.tiltakspenger.felles
/*
import com.google.common.collect.BoundType
import com.google.common.collect.DiscreteDomain
import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet
import java.time.LocalDate

// TODO: Needs more work
class LocalDateDiscreteDomain : DiscreteDomain<LocalDate>() {
    override fun next(value: LocalDate): LocalDate {
        return value.plusDays(1)
    }

    override fun previous(value: LocalDate): LocalDate {
        return value.minusDays(1)
    }

    override fun distance(start: LocalDate, end: LocalDate): Long {
        return start.until(end).days.toLong()
    }
}

class Periode(fra: LocalDate, til: LocalDate) {

    companion object {
        val domain = LocalDateDiscreteDomain()
    }

    val range: Range<LocalDate> = lagRangeFraFraOgTil(fra, til)

    private fun lagRangeFraFraOgTil(fra: LocalDate, til: LocalDate): Range<LocalDate> =
        when {
            fra == LocalDate.MIN && til == LocalDate.MAX -> Range.all<LocalDate>().canonical(domain)
            fra == LocalDate.MIN && til != LocalDate.MAX -> Range.atMost(til).canonical(domain)
            fra != LocalDate.MIN && til == LocalDate.MAX -> Range.atLeast(fra).canonical(domain)
            else -> Range.closed(fra, til).canonical(domain)
        }

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

    fun overlappendePeriode(periode: Periode): Periode? = try {
        this.range.intersection(periode.range).toPeriode()
    } catch (e: Exception) {
        null
    }

    // TODO: Trenger tester!
    fun overlappenderPerioder(perioder: List<Periode>): List<Periode> {
        val rangeSet: RangeSet<LocalDate> = TreeRangeSet.create()
        perioder.forEach { periode -> this.overlappendePeriode(periode)?.range.let { rangeSet.add(it!!) } }
        return rangeSet.asRanges().toPerioder()
    }

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

    fun inneholder(dato: LocalDate): Boolean = range.contains(dato)

    fun etter(dato: LocalDate): Boolean = this.fra.isAfter(dato)

    fun før(dato: LocalDate): Boolean = this.til.isBefore(dato)

    fun trekkFra(andrePerioder: List<Periode>): List<Periode> {
        val opprinneligeRangeSet =
            ImmutableRangeSet.Builder<LocalDate>().add(this.range).build()
        val andrePeriodeRangeSet =
            ImmutableRangeSet.Builder<LocalDate>().addAll(andrePerioder.map { it.range }).build()
        val ranges = opprinneligeRangeSet.difference(andrePeriodeRangeSet).asRanges()
        return ranges.filter { !it.canonical(domain).isEmpty }.map { it.toPeriode() }
    }

    fun tilDager(): List<LocalDate> {
        return fra.datesUntil(til.plusDays(1)).toList()
    }
}

fun List<Periode>.inneholderOverlapp(): Boolean {
    val rangeSet = TreeRangeSet.create<LocalDate>()
    this.forEach {
        if (rangeSet.intersects(it.range)) {
            return true
        } else {
            rangeSet.add(it.range)
        }
    }
    return false
}

fun List<Periode>.leggSammen(godtaOverlapp: Boolean = true): List<Periode> {
    if (!godtaOverlapp && this.inneholderOverlapp()) {
        throw IllegalArgumentException("Listen inneholder overlappende perioder")
    }
    val rangeSet = TreeRangeSet.create<LocalDate>()
    rangeSet.addAll(this.map { it.range })
    return rangeSet.asRanges().toPerioder()
}

fun List<Periode>.trekkFra(perioder: List<Periode>): List<Periode> {
    val rangeSet = TreeRangeSet.create<LocalDate>()
    rangeSet.addAll(this.map { it.range })
    rangeSet.removeAll(perioder.map { it.range })
    return rangeSet.asRanges().toPerioder()
}

fun Set<Range<LocalDate>>.toPerioder() = this.map { it.toPeriode() }
fun Range<LocalDate>.toPeriode(): Periode = Periode(this.fraOgMed(), this.tilOgMed())
fun Range<LocalDate>.fraOgMed(): LocalDate =
    if (this.hasLowerBound()) {
        if (this.lowerBoundType() == BoundType.CLOSED) this.lowerEndpoint() else this.lowerEndpoint().plusDays(1)
    } else {
        LocalDate.MIN
    }

fun Range<LocalDate>.tilOgMed(): LocalDate =
    if (this.hasUpperBound()) {
        if (this.upperBoundType() == BoundType.CLOSED) this.upperEndpoint() else this.upperEndpoint().minusDays(1)
    } else {
        LocalDate.MAX
    }


 */
