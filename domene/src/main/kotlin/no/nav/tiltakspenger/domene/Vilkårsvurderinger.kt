package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.Faktum
import no.nav.tiltakspenger.domene.fakta.Fakta

data class Vilkårsvurderinger (
    val periode: Periode,
    val vilkårsvurderinger: List<Vilkårsvurdering<out Fakta<out Faktum>, out Faktum>>,
) {
    init {
        require(vilkårsvurderinger
            .map { it.vurderingsperiode }
            .all { periode == it })
    }
}
