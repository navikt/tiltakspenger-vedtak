package no.nav.tiltakspenger.felles.temp

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.leggSammen
import no.nav.tiltakspenger.felles.leggSammenMed

data class PeriodeMedVerdi<T>(
    val verdi: T,
    val periode: Periode,
)

data class PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T> private constructor(
    private val totalePeriode: Periode,
    private val initielleVerdiForHelePeriode: T,
    private val perioderMedVerdi: List<PeriodeMedVerdi<T>>,
) {

    companion object {
        operator fun <T> invoke(
            initielleVerdiForHelePeriode: T,
            totalePeriode: Periode,
        ): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T> {
            return PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                totalePeriode = totalePeriode,
                initielleVerdiForHelePeriode = initielleVerdiForHelePeriode,
                perioderMedVerdi =
                listOf(PeriodeMedVerdi(initielleVerdiForHelePeriode, totalePeriode)),
            )
        }

        fun <T> kombinerLike(
            liste: List<PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T>>,
            sammensattVerdi: (T, T) -> T,
        ): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T> {
            return liste.reduce { total, next ->
                total.kombiner(next, sammensattVerdi).slåSammenPerioderForVerdierSomErLike()
            }
        }
    }

    fun perioder(): List<PeriodeMedVerdi<T>> = perioderMedVerdi.sortedBy { it.periode.fra }

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T> List<PeriodeMedVerdi<T>>.leggTil(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        this.map { it.periode }.leggSammenMed(periodeMedVerdi.periode)
            .map { PeriodeMedVerdi(periodeMedVerdi.verdi, it) }

    private fun <T> List<PeriodeMedVerdi<T>>.trekkFra(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        this.flatMap {
            it.periode.trekkFra(listOf(periodeMedVerdi.periode))
                .map { nyPeriode -> PeriodeMedVerdi(it.verdi, nyPeriode) }
        }

    fun erstattSubPeriodeMedVerdi(
        subPeriodensVerdi: T,
        subPeriode: Periode,
    ): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T> {
        if (!totalePeriode.inneholderHele(subPeriode)) {
            throw IllegalArgumentException("Angitt periode er ikke innenfor $totalePeriode")
        }
        val nyePerioderMedSammeVerdi = perioderMedVerdi.filter { it.verdi == subPeriodensVerdi }
            .leggTil(PeriodeMedVerdi(subPeriodensVerdi, subPeriode))
        val nyePerioderMedUlikVerdi = perioderMedVerdi.filter { it.verdi != subPeriodensVerdi }
            .trekkFra(PeriodeMedVerdi(subPeriodensVerdi, subPeriode))
        return PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
            totalePeriode,
            initielleVerdiForHelePeriode,
            nyePerioderMedSammeVerdi + nyePerioderMedUlikVerdi,
        )
    }

    fun <U, V> kombiner(
        other: PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<U>,
        sammensattVerdi: (T, U) -> V,
    ): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<V> {
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
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                this.totalePeriode,
                sammensattVerdi(this.initielleVerdiForHelePeriode, other.initielleVerdiForHelePeriode),
                it,
            )
        }
    }

    fun slåSammenPerioderForVerdierSomErLike(): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<T> =
        PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
            this.totalePeriode,
            this.initielleVerdiForHelePeriode,
            perioderMedVerdi.slåSammenPerioderForVerdierSomErLike(),
        )

    private fun <T> List<PeriodeMedVerdi<T>>.slåSammenPerioderForVerdierSomErLike(): List<PeriodeMedVerdi<T>> =
        this.groupBy { it.verdi }
            .flatMap { entry ->
                entry.value.map { it.periode }.leggSammen(true).map { PeriodeMedVerdi(entry.key, it) }
            }.toList()

    fun <U> splitt(
        ekstrahertVerdi: (T) -> U,
    ): PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<U> =
        this.perioderMedVerdi.map {
            PeriodeMedVerdi(ekstrahertVerdi(it.verdi), it.periode)
        }.slåSammenPerioderForVerdierSomErLike().let {
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                this.totalePeriode,
                ekstrahertVerdi(this.initielleVerdiForHelePeriode),
                it,
            )
        }
}
