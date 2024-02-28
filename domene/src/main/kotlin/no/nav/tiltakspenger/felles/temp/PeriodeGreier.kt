package no.nav.tiltakspenger.felles.temp

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.leggSammen
import no.nav.tiltakspenger.felles.leggSammenMed
import no.nav.tiltakspenger.felles.overlappendePerioder
import no.nav.tiltakspenger.felles.trekkFra

// Er det litt dumt at den wrapper List? Burde den delegere til List? Eller extende List?
class IkkeOverlappendePerioder(perioder: List<Periode>) {

    // Er det bedre med en check her for å verifisere at periodene faktisk er ikke-overlappende?
    // Kan bruke perioder.inneholderOverlapp()
    private val perioder: List<Periode> = perioder.leggSammen(true)

    fun erTom(): Boolean = perioder.isEmpty()

    fun leggTil(periode: Periode): IkkeOverlappendePerioder =
        IkkeOverlappendePerioder(perioder = perioder.leggSammenMed(periode, true))

    fun leggTil(andreIkkeOverlappendePerioder: IkkeOverlappendePerioder): IkkeOverlappendePerioder =
        IkkeOverlappendePerioder(perioder = perioder.leggSammenMed(andreIkkeOverlappendePerioder.perioder, true))

    fun trekkFra(periode: Periode): IkkeOverlappendePerioder =
        IkkeOverlappendePerioder(perioder = perioder.trekkFra(listOf(periode)))

    fun trekkFra(andreIkkeOverlappendePerioder: IkkeOverlappendePerioder): IkkeOverlappendePerioder =
        IkkeOverlappendePerioder(perioder = perioder.trekkFra(andreIkkeOverlappendePerioder.perioder))

    fun perioder(): List<Periode> = perioder.sortedBy { it.fra }

    fun overlappende(other: IkkeOverlappendePerioder): IkkeOverlappendePerioder =
        IkkeOverlappendePerioder(this.perioder.overlappendePerioder(other.perioder))

    override fun toString(): String {
        return "IkkeOverlappendePerioder(perioder=$perioder)"
    }
}

class IkkeOverlappendePerioderMedSammeVerdi<T>(
    val verdi: T,
    val perioder: IkkeOverlappendePerioder,
) {
    fun leggTil(periode: Periode): IkkeOverlappendePerioderMedSammeVerdi<T> =
        IkkeOverlappendePerioderMedSammeVerdi(this.verdi, this.perioder.leggTil(periode))

    fun leggTil(andre: IkkeOverlappendePerioder): IkkeOverlappendePerioderMedSammeVerdi<T> =
        IkkeOverlappendePerioderMedSammeVerdi(this.verdi, this.perioder.leggTil(andre))

    fun trekkFra(periode: Periode) =
        IkkeOverlappendePerioderMedSammeVerdi(this.verdi, this.perioder.trekkFra(periode))

    fun harVerdienPerioder(): Boolean = !this.perioder.erTom()

    override fun toString(): String {
        return "IkkeOverlappendePerioderMedSammeVerdi(verdi=$verdi, perioder=$perioder)"
    }
}

class IkkeOverlappendePerioderMedUlikeVerdier<T> private constructor(
    val totalePeriode: Periode,
    val initielleVerdiForHelePeriode: T,
    val perioderMedUlikVerdi: List<IkkeOverlappendePerioderMedSammeVerdi<T>>, // Burde dette vært en map?
) {

    companion object {
        operator fun <T> invoke(
            initielleVerdiForHelePeriode: T,
            totalePeriode: Periode,
        ): IkkeOverlappendePerioderMedUlikeVerdier<T> {
            return IkkeOverlappendePerioderMedUlikeVerdier(
                totalePeriode = totalePeriode,
                initielleVerdiForHelePeriode = initielleVerdiForHelePeriode,
                perioderMedUlikVerdi =
                listOf(
                    IkkeOverlappendePerioderMedSammeVerdi(
                        initielleVerdiForHelePeriode,
                        IkkeOverlappendePerioder(listOf(totalePeriode)),
                    ),
                ),
            )
        }

        fun <T> kombinerLike(
            liste: List<IkkeOverlappendePerioderMedUlikeVerdier<T>>,
            sammensattVerdi: (T, T) -> T,
        ): IkkeOverlappendePerioderMedUlikeVerdier<T> {
            return liste.reduce { total, next ->
                total.kombiner(next, sammensattVerdi).slåSammenUlikeVerdierSomErLike()
            }
        }

        private fun <T> IkkeOverlappendePerioderMedUlikeVerdier<T>.slåSammenUlikeVerdierSomErLike():
            IkkeOverlappendePerioderMedUlikeVerdier<T> {
            return this.perioderMedUlikVerdi
                .groupBy { it.verdi }
                .mapValues {
                    it.value.reduce { total, next ->
                        total.leggTil(next.perioder)
                    }
                }
                .let {
                    IkkeOverlappendePerioderMedUlikeVerdier(
                        this.totalePeriode,
                        this.initielleVerdiForHelePeriode,
                        it.values.toList(),
                    )
                }
        }
    }

    fun erstattSubPeriodeMedVerdi(
        subPeriodensVerdi: T,
        subPeriode: Periode,
    ): IkkeOverlappendePerioderMedUlikeVerdier<T> {
        if (!totalePeriode.inneholderHele(subPeriode)) {
            throw IllegalArgumentException("Angitt periode er ikke innenfor $totalePeriode")
        }
        val nyePerioderMedSammeVerdi = if (perioderMedUlikVerdi.none { it.verdi == subPeriodensVerdi }) {
            listOf(
                IkkeOverlappendePerioderMedSammeVerdi(
                    subPeriodensVerdi,
                    IkkeOverlappendePerioder(listOf(subPeriode)),
                ),
            )
        } else {
            perioderMedUlikVerdi
                .filter { it.verdi == subPeriodensVerdi }
                .map { it.leggTil(subPeriode) }
        }
        val nyePerioderMedUlikVerdi: List<IkkeOverlappendePerioderMedSammeVerdi<T>> = perioderMedUlikVerdi
            .filter { it.verdi != subPeriodensVerdi }
            .map { it.trekkFra(subPeriode) }
            .filter { it.harVerdienPerioder() }
        return IkkeOverlappendePerioderMedUlikeVerdier(
            totalePeriode,
            initielleVerdiForHelePeriode,
            nyePerioderMedSammeVerdi + nyePerioderMedUlikVerdi,
        )
    }

    fun <U, V> kombiner(
        other: IkkeOverlappendePerioderMedUlikeVerdier<U>,
        sammensattVerdi: (T, U) -> V,
    ): IkkeOverlappendePerioderMedUlikeVerdier<V> {
        if (totalePeriode != other.totalePeriode) {
            throw IllegalArgumentException("Perioder som skal kombineres må være like")
        }

        val ikkeOverlappendePerioderMedUlikeVerdier: List<IkkeOverlappendePerioderMedSammeVerdi<V>> =
            this.perioderMedUlikVerdi.flatMap { thisIkkeOverlappendePerioderMedSammeVerdi ->
                other.perioderMedUlikVerdi.map { otherIkkeOverlappendePerioderMedSammeVerdi ->
                    IkkeOverlappendePerioderMedSammeVerdi(
                        sammensattVerdi(
                            thisIkkeOverlappendePerioderMedSammeVerdi.verdi,
                            otherIkkeOverlappendePerioderMedSammeVerdi.verdi,
                        ),
                        thisIkkeOverlappendePerioderMedSammeVerdi.perioder.overlappende(
                            otherIkkeOverlappendePerioderMedSammeVerdi.perioder,
                        ),
                    )
                }
            }.filter {
                it.harVerdienPerioder()
            }
        return IkkeOverlappendePerioderMedUlikeVerdier(
            totalePeriode,
            sammensattVerdi(this.initielleVerdiForHelePeriode, other.initielleVerdiForHelePeriode),
            ikkeOverlappendePerioderMedUlikeVerdier,
        )
    }

    fun <U> splitt(
        ekstrahertVerdi: (T) -> U,
    ): IkkeOverlappendePerioderMedUlikeVerdier<U> {
        return this.perioderMedUlikVerdi
            .map {
                IkkeOverlappendePerioderMedSammeVerdi(ekstrahertVerdi(it.verdi), it.perioder)
            }
            .groupBy { it.verdi }
            .mapValues {
                it.value.reduce { total, next ->
                    total.leggTil(next.perioder)
                }
            }
            .let {
                IkkeOverlappendePerioderMedUlikeVerdier<U>(
                    this.totalePeriode,
                    ekstrahertVerdi(this.initielleVerdiForHelePeriode),
                    it.values.toList(),
                )
            }
    }

    override fun toString(): String {
        return "IkkeOverlappendePerioderMedUlikeVerdier(perioderMedUlikVerdi=$perioderMedUlikVerdi)"
    }
}
