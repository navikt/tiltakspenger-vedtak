package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering

class LønnsinntektVilkårsvurderingKategori(
    val lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.LØNNSINNTEKT

    override fun samletUtfall(): Utfall =
        lønnsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        lønnsinntektVilkårsvurdering.vurderinger()
}
