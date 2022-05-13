package no.nav.tiltakspenger.domene

enum class Utfall {
    IKKE_VURDERT,
    VURDERT_OG_OPPFYLT,
    VURDERT_OG_IKKE_OPPFYLT,
    VURDERT_OG_TRENGER_MANUELL_VURDERING
}

data class Vilkårsvurdering(
    val utfall: Utfall = Utfall.IKKE_VURDERT,
    val vilkår: Vilkår,
    val fakta: List<Faktum> = emptyList(),
    val vurderingsperiode: Periode
) {
    fun vurder(faktum: Faktum): Vilkårsvurdering {
        val oppdaterteFakta = fakta + listOf(faktum).filter { faktum -> faktum.erRelevantFor(vilkår) }
        return this.copy(
            utfall = vilkår.vurder(oppdaterteFakta),
            fakta = oppdaterteFakta,
        )
    }
}

fun List<Vilkårsvurdering>.erInngangsVilkårOppfylt(): Boolean = this
    .filter { it.vilkår.erInngangsVilkår }
    .all { it.utfall == Utfall.VURDERT_OG_OPPFYLT }
