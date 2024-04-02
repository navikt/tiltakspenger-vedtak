package no.nav.tiltakspenger.domene.vilkår.temp

import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Utfall

data class PeriodeMedAvklarteFakta(
    val periodeMedVerdier: PeriodeMedVerdier<Saksopplysning>,
) {

    fun lagVurdering(): PeriodeMedVurderinger =
        PeriodeMedVurderinger(
            periodeMedVerdier.map {
                val utfall = when (it.typeSaksopplysning) {
                    TypeSaksopplysning.IKKE_INNHENTET_ENDA -> Utfall.KREVER_MANUELL_VURDERING
                    TypeSaksopplysning.HAR_YTELSE -> Utfall.IKKE_OPPFYLT
                    TypeSaksopplysning.HAR_IKKE_YTELSE -> Utfall.OPPFYLT
                }
                Vurdering(
                    utfall = utfall,
                    vilkår = it.vilkår,
                    kilde = it.kilde,
                )
            },
        )
}
