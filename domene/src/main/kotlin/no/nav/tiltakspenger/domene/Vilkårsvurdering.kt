package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.Faktum

data class Vilkårsvurdering(
    val vilkår: Vilkår,
    val fakta: List<Faktum> = emptyList(),
    val vurderingsperiode: Periode,
    val utfall: List<Utfall> = listOf(Utfall.IkkeVurdert(periode = vurderingsperiode)),
) {
    fun vurder(faktum: Faktum): Vilkårsvurdering {
        val oppdaterteFakta = fakta + listOf(faktum).filter { it.erRelevantFor(vilkår) }
        return this.copy(
            utfall = vilkår.vurder(oppdaterteFakta, vurderingsperiode),
            fakta = oppdaterteFakta,
        )
    }
}

//fun List<Vilkårsvurdering>.erInngangsVilkårOppfylt(): Boolean = this
//    .filter { it.vilkår.erInngangsVilkår }
//    .all { it.utfall is Utfall.VurdertOgOppfylt }
