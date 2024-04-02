package no.nav.tiltakspenger.domene.vilkår.temp

data class PeriodeMedSaksopplysningerForEnKilde(val periodeMedVerdier: PeriodeMedVerdier<Saksopplysning>) {

    fun leggTilSaksopplysning(saksopplysning: PeriodeMedVerdi<Saksopplysning>): PeriodeMedSaksopplysningerForEnKilde {
        check(saksopplysning.verdi.vilkår == periodeMedVerdier.perioder().first().verdi.vilkår)
        check(saksopplysning.verdi.kilde == periodeMedVerdier.perioder().first().verdi.kilde)
        check(periodeMedVerdier.totalePeriode.inneholderHele(saksopplysning.periode))
        return this.copy(
            periodeMedVerdier = periodeMedVerdier.setPeriodeMedVerdi(saksopplysning),
        )
    }

    companion object {
        operator fun invoke(
            defaultSaksopplysning: SaksopplysningFraKilde,
            periode: Periode,
        ): PeriodeMedSaksopplysningerForEnKilde {
            return PeriodeMedSaksopplysningerForEnKilde(
                periodeMedVerdier = PeriodeMedVerdier(
                    defaultVerdi = defaultSaksopplysning,
                    totalePeriode = periode,
                ),
            )
        }
    }
}
