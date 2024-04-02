package no.nav.tiltakspenger.domene.vilkår.temp

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class PeriodeMedSaksopplysninger private constructor(
    private val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val map: Map<Kilde, PeriodeMedSaksopplysningerForEnKilde> =
        mapOf(
            Kilde.SAKSB to
                PeriodeMedSaksopplysningerForEnKilde(
                    TomSaksopplysning(vilkår, Kilde.SAKSB),
                    vurderingsperiode,
                ),
        ),
) {

    fun leggTilSaksopplysning(saksopplysning: PeriodeMedVerdi<Saksopplysning>): PeriodeMedSaksopplysninger {
        check(saksopplysning.verdi.vilkår == vilkår)
        check(vurderingsperiode.inneholderHele(saksopplysning.periode))
        val entryForKilde = map[saksopplysning.verdi.kilde] ?: PeriodeMedSaksopplysningerForEnKilde(
            TomSaksopplysning(saksopplysning.verdi.vilkår, saksopplysning.verdi.kilde),
            vurderingsperiode,
        )
        return this.copy(
            map = map + (saksopplysning.verdi.kilde to entryForKilde.leggTilSaksopplysning(saksopplysning)),
        )
    }

    fun avklarFakta(): PeriodeMedAvklarteFakta {
        return PeriodeMedAvklarteFakta(
            PeriodeMedVerdier.kombinerLikePerioderMedSammeTypeVerdier(
                map.values.map { it.periodeMedVerdier }.toList(),
            ) { saksopplysning1, saksopplysning2 ->
                // TODO Vi har noen algoritmer for å prioritere SBH over annet
                saksopplysning1
            },
        )
    }

    companion object {
        operator fun invoke(
            vilkår: Vilkår,
            vurderingsperiode: Periode,
        ) = PeriodeMedSaksopplysninger(vilkår, vurderingsperiode)
    }
}
