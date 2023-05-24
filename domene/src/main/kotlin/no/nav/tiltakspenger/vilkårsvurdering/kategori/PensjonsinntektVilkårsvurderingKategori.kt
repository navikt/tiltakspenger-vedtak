package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PrivatPensjonsinntektVilkårsvurdering

class PensjonsinntektVilkårsvurderingKategori(
    val privatPensjonsinntektVilkårsvurdering: PrivatPensjonsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.PENSJONSINNTEKT

    override fun samletUtfall(): Utfall =
        privatPensjonsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        privatPensjonsinntektVilkårsvurdering.vurderinger()
}
