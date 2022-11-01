package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class PensjonsinntektVilkårsvurderingKategori(
    val pensjonsinntektVilkårsvurdering: PensjonsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.PENSJONSINNTEKT

    override fun samletUtfall(): Utfall =
        pensjonsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        pensjonsinntektVilkårsvurdering.vurderinger()
}
