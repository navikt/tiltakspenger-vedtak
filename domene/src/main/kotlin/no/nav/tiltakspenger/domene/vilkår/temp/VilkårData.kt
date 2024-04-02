package no.nav.tiltakspenger.domene.vilkår.temp

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class VilkårData private constructor(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val saksopplysninger: PeriodeMedSaksopplysninger,
    val avklartFakta: PeriodeMedAvklarteFakta,
    val vurderinger: PeriodeMedVurderinger,
) {
    companion object {
        operator fun invoke(
            vilkår: Vilkår,
            vurderingsperiode: Periode,
        ): VilkårData {
            val periodeMedSaksopplysninger = PeriodeMedSaksopplysninger(
                vilkår,
                vurderingsperiode,
            )
            return VilkårData(
                vilkår = vilkår,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = periodeMedSaksopplysninger,
                avklartFakta = periodeMedSaksopplysninger.avklarFakta(),
                vurderinger = periodeMedSaksopplysninger.avklarFakta().lagVurdering(),
            )
        }
    }
}
