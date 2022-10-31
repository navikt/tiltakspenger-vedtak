package no.nav.tiltakspenger.vilkårsvurdering

class InstitusjonVilkårsvurderingKategori(
    val institusjonsoppholdVilkårsvurdering: InstitusjonsoppholdVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD

    override fun samletUtfall(): Utfall =
        institusjonsoppholdVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        institusjonsoppholdVilkårsvurdering.vurderinger()
}
