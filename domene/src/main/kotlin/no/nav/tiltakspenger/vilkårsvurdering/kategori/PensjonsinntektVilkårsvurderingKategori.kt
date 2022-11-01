package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering

class PensjonsinntektVilkårsvurderingKategori(
    val pensjonsinntektVilkårsvurdering: PensjonsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.PENSJONSINNTEKT

    override fun samletUtfall(): Utfall =
        pensjonsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        pensjonsinntektVilkårsvurdering.vurderinger()
}
