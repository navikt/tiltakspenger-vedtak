package no.nav.tiltakspenger.felles.temp

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.leggSammen
import no.nav.tiltakspenger.felles.leggSammenMed

/*
Denne klassen representerer en sammenhengende periode som har samme verdi for hele perioden.
Perioden kan ikke ha "hull" som ikke har en verdi
 */
data class PeriodeMedVerdi<T>(
    val verdi: T,
    val periode: Periode,
)

/*
Denne klassen representerer en sammenhengende periode som kan ha ulike verdier for ulike deler av perioden.
Perioden kan ikke ha "hull" som ikke har en verdi
 */
data class PeriodeMedVerdier<T> private constructor(
    private val totalePeriode: Periode,
    private val defaultVerdi: T,
    private val perioderMedVerdi: List<PeriodeMedVerdi<T>>,
) {

    companion object {
        operator fun <T> invoke(
            defaultVerdi: T,
            totalePeriode: Periode,
        ): PeriodeMedVerdier<T> {
            return PeriodeMedVerdier(
                totalePeriode = totalePeriode,
                defaultVerdi = defaultVerdi,
                perioderMedVerdi =
                listOf(PeriodeMedVerdi(defaultVerdi, totalePeriode)),
            )
        }

        fun <T> kombinerLike(
            liste: List<PeriodeMedVerdier<T>>,
            sammensattVerdi: (T, T) -> T,
        ): PeriodeMedVerdier<T> {
            return liste.reduce { total, next ->
                total.kombiner(next, sammensattVerdi).slåSammenTilstøtendePerioder()
            }
        }
    }

    fun perioder(): List<PeriodeMedVerdi<T>> = perioderMedVerdi.sortedBy { it.periode.fra }

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T> List<PeriodeMedVerdi<T>>.leggTilPeriodeMedSammeVerdi(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        this.map { it.periode }.leggSammenMed(periodeMedVerdi.periode)
            .map { PeriodeMedVerdi(periodeMedVerdi.verdi, it) }

    // Her bryr vi oss ikke om verdien til perioden som trekkes fra
    private fun <T> List<PeriodeMedVerdi<T>>.trekkFra(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        this.flatMap {
            it.periode.trekkFra(listOf(periodeMedVerdi.periode))
                .map { nyPeriode -> PeriodeMedVerdi(it.verdi, nyPeriode) }
        }

    private fun <T> List<PeriodeMedVerdi<T>>.perioderMedSammeVerdi(verdi: T): List<PeriodeMedVerdi<T>> =
        this.filter { it.verdi == verdi }

    private fun <T> List<PeriodeMedVerdi<T>>.perioderMedUlikVerdi(verdi: T): List<PeriodeMedVerdi<T>> =
        this.filter { it.verdi != verdi }

    private fun <T> List<PeriodeMedVerdi<T>>.slåSammenTilstøtendePerioder(): List<PeriodeMedVerdi<T>> =
        this.groupBy { it.verdi }
            .values
            .flatMap { listeMedLikeVerdier ->
                listeMedLikeVerdier.slåSammenTilstøtendePerioderMedSammeVerdi()
            }

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T> List<PeriodeMedVerdi<T>>.slåSammenTilstøtendePerioderMedSammeVerdi(): List<PeriodeMedVerdi<T>> {
        if (this.isEmpty()) {
            return this
        }
        if (this.map { it.verdi }.distinct().size > 1) {
            throw IllegalArgumentException("Kan ikke så sammen perioder med ulike verdier")
        }
        val verdi: T = this.firstOrNull()!!.verdi
        return this.map { it.periode }.leggSammen(false).map { PeriodeMedVerdi(verdi, it) }
    }

    fun slåSammenTilstøtendePerioder(): PeriodeMedVerdier<T> =
        this.copy(perioderMedVerdi = perioderMedVerdi.slåSammenTilstøtendePerioder())

    fun setDelPeriodeMedVerdi(delPeriodeMedVerdi: PeriodeMedVerdi<T>): PeriodeMedVerdier<T> {
        if (!totalePeriode.inneholderHele(delPeriodeMedVerdi.periode)) {
            throw IllegalArgumentException("Angitt periode er ikke innenfor $totalePeriode")
        }
        val nyePerioderMedSammeVerdi = perioderMedVerdi
            .perioderMedSammeVerdi(delPeriodeMedVerdi.verdi)
            .leggTilPeriodeMedSammeVerdi(delPeriodeMedVerdi)
        val nyePerioderMedUlikVerdi = perioderMedVerdi
            .perioderMedUlikVerdi(delPeriodeMedVerdi.verdi)
            .trekkFra(delPeriodeMedVerdi)
        return PeriodeMedVerdier(
            totalePeriode,
            defaultVerdi,
            nyePerioderMedSammeVerdi + nyePerioderMedUlikVerdi,
        )
    }

    fun setDelPeriodeMedVerdi(
        verdi: T,
        delPeriode: Periode,
    ): PeriodeMedVerdier<T> = setDelPeriodeMedVerdi(PeriodeMedVerdi(verdi, delPeriode))

    fun <U, V> kombiner(
        other: PeriodeMedVerdier<U>,
        sammensattVerdi: (T, U) -> V,
    ): PeriodeMedVerdier<V> {
        if (totalePeriode != other.totalePeriode) {
            throw IllegalArgumentException("Perioder som skal kombineres må være like")
        }

        return this.perioderMedVerdi.flatMap { thisPeriodeMedVerdi ->
            other.perioderMedVerdi.mapNotNull { otherPeriodeMedVerdi ->
                thisPeriodeMedVerdi.periode.overlappendePeriode(otherPeriodeMedVerdi.periode)?.let {
                    PeriodeMedVerdi(sammensattVerdi(thisPeriodeMedVerdi.verdi, otherPeriodeMedVerdi.verdi), it)
                }
            }
        }.let {
            PeriodeMedVerdier(
                this.totalePeriode,
                sammensattVerdi(this.defaultVerdi, other.defaultVerdi),
                it,
            )
        }
    }

    fun <U> splitt(
        ekstrahertVerdi: (T) -> U,
    ): PeriodeMedVerdier<U> =
        this.perioderMedVerdi
            .map { PeriodeMedVerdi(ekstrahertVerdi(it.verdi), it.periode) }
            .slåSammenTilstøtendePerioder()
            .let { PeriodeMedVerdier(this.totalePeriode, ekstrahertVerdi(this.defaultVerdi), it) }
}
