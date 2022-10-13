package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vedtak.Søknad

//enum class Utfall {
//    VURDERT_OK,
//    VURDERT_IKKE_OK,
//    IKKE_VURDERDT
//}

data class Vurdering(
//    val utfall: Utfall,
//    val periode: Periode,
    val kilde: String,
)

class IntroProgrammetVilkårsvurdering(
    søknad: Søknad
) {
    val vurderinger: List<Vurdering> = listOf(Vurdering(kilde = "Søknad"))

}
