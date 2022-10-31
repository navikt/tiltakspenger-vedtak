package no.nav.tiltakspenger.vilkårsvurdering

class LønnsinntektVilkårsvurderingKategori(
    val lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.LØNNSINNTEKT

    override fun samletUtfall(): Utfall =
        lønnsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        lønnsinntektVilkårsvurdering.vurderinger()
}
