package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class LønnsinntektVilkårsvurderingKategori(
    val lønnsinntektVilkårsvurdering: LønnsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.LØNNSINNTEKT

    override fun samletUtfall(): Utfall =
        lønnsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        lønnsinntektVilkårsvurdering.vurderinger()
}
