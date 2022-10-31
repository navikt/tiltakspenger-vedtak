package no.nav.tiltakspenger.vilkårsvurdering


class Vilkårsvurderinger(
    val statligeYtelser: StatligeYtelserVilkårsvurderinger,
    val kommunaleYtelser: KommunaleYtelserVilkårsvurderinger,
    val pensjonsordninger: VilkårsvurderingKategori,
    val lønnsinntekt: VilkårsvurderingKategori,
    val institusjonopphold: VilkårsvurderingKategori,
) {
    fun samletUtfall(): Utfall {
        val utfall =
            listOf(
                statligeYtelser.samletUtfall(),
                kommunaleYtelser.samletUtfall(),
                pensjonsordninger.samletUtfall(),
                lønnsinntekt.samletUtfall(),
                institusjonopphold.samletUtfall(),
            )
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    fun vurderinger(): List<Vurdering> =
        listOf(
            statligeYtelser.vurderinger(),
            kommunaleYtelser.vurderinger(),
            pensjonsordninger.vurderinger(),
            lønnsinntekt.vurderinger(),
            institusjonopphold.vurderinger(),
        ).flatten()
}

fun List<Vurdering>.ikkeOppfylte() = this.filter { it.utfall == Utfall.IKKE_OPPFYLT }
