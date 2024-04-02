package no.nav.tiltakspenger.domene.vilkår.temp

/*
Denne klassen representerer en sammenhengende periode som kan ha ulike verdier for ulike deler av perioden.
Perioden kan ikke ha "hull" som ikke har en verdi
 */
data class PeriodeMedVerdier<T : Sammenlignbar<T>> private constructor(
    val totalePeriode: Periode,
    private val defaultVerdi: T,
    private val perioderMedVerdi: List<PeriodeMedVerdi<T>>,
) {
    companion object {
        operator fun <T : Sammenlignbar<T>> invoke(
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

        fun <T : Sammenlignbar<T>> kombinerLikePerioderMedSammeTypeVerdier(
            liste: List<PeriodeMedVerdier<T>>,
            sammensattVerdi: (T, T) -> T,
        ): PeriodeMedVerdier<T> {
            return liste.reduce { total, next ->
                total.kombiner(next, sammensattVerdi).slåSammenTilstøtendePerioder()
            }
        }
    }

    // Offentlig API:

    fun slåSammenTilstøtendePerioder(): PeriodeMedVerdier<T> =
        this.copy(perioderMedVerdi = perioderMedVerdi.slåSammenTilstøtendePerioder())

    fun setVerdiForDelPeriode(
        verdi: T,
        delPeriode: Periode,
    ): PeriodeMedVerdier<T> = setPeriodeMedVerdi(PeriodeMedVerdi(verdi, delPeriode))

    fun <U : Sammenlignbar<U>, V : Sammenlignbar<V>> kombiner(
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

    fun <U : Sammenlignbar<U>> map(
        ekstrahertVerdi: (T) -> U,
    ): PeriodeMedVerdier<U> =
        this.perioderMedVerdi
            .map { PeriodeMedVerdi(ekstrahertVerdi(it.verdi), it.periode) }
            .slåSammenTilstøtendePerioder()
            .let { PeriodeMedVerdier(this.totalePeriode, ekstrahertVerdi(this.defaultVerdi), it) }

    fun perioder(): List<PeriodeMedVerdi<T>> = perioderMedVerdi.sortedBy { it.periode.fra }

    fun setPeriodeMedVerdi(delPeriodeMedVerdi: PeriodeMedVerdi<T>): PeriodeMedVerdier<T> {
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

    // Private hjelpemetoder:

    // Krever IKKE at alle elementer i lista har samme verdi for å fungere
    private fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.trekkFra(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        this.trekkFra(periodeMedVerdi.periode)

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.leggSammenPerioderMedSammeVerdi(godtaOverlapp: Boolean = true): List<PeriodeMedVerdi<T>> {
        if (!this.allePerioderHarSammeVerdi()) {
            throw IllegalArgumentException("Kan bare legge sammen perioder med samme verdi")
        }
        if (!godtaOverlapp && this.map { it.periode }.inneholderOverlapp()) {
            throw IllegalArgumentException("Listen inneholder overlappende perioder")
        }
        val verdi: T = this.firstOrNull()!!.verdi
        return this.map { it.periode }.leggSammen(false).map { PeriodeMedVerdi(verdi, it) }
    }

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.slåSammenTilstøtendePerioderMedSammeVerdi(): List<PeriodeMedVerdi<T>> =
        this.leggSammenPerioderMedSammeVerdi(false)

    // Krever IKKE at alle elementer i lista har samme verdi for å fungere
    private fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.slåSammenTilstøtendePerioder(): List<PeriodeMedVerdi<T>> =
        this.groupBy { it.verdi }
            .values
            .flatMap { listeMedLikeVerdier -> listeMedLikeVerdier.slåSammenTilstøtendePerioderMedSammeVerdi() }

    // Krever at alle elementer i lista har samme verdi for å fungere!
    private fun <T : Sammenlignbar<T>> List<PeriodeMedVerdi<T>>.leggTilPeriodeMedSammeVerdi(periodeMedVerdi: PeriodeMedVerdi<T>): List<PeriodeMedVerdi<T>> =
        (this + periodeMedVerdi).leggSammenPerioderMedSammeVerdi(true)

    override fun toString(): String {
        return "PeriodeMedVerdier(totalePeriode=$totalePeriode, defaultVerdi=$defaultVerdi, perioderMedVerdi=${
            perioderMedVerdi.sortedBy { it.periode.fra }.map { "\n" + it.toString() }
        })"
    }
}
