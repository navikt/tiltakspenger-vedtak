package no.nav.tiltakspenger.vilkårsvurdering


class Vilkårsvurderinger(
    val statligeYtelserVilkårsvurderinger: StatligeYtelserVilkårsvurderinger,
    val kommunaleYtelserVilkårsvurderinger: KommunaleYtelserVilkårsvurderinger,
) {
    fun samletUtfall(): Utfall {
        val utfall =
            listOf(statligeYtelserVilkårsvurderinger.samletUtfall(), kommunaleYtelserVilkårsvurderinger.samletUtfall())
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    fun vurderinger(): List<Vurdering> =
        listOf(
            statligeYtelserVilkårsvurderinger.vurderinger(),
            kommunaleYtelserVilkårsvurderinger.vurderinger()
        ).flatten()
}

fun List<Vurdering>.ikkeOppfylte() = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
