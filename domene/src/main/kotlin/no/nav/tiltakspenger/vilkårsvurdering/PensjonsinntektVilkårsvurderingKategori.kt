package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT

class PensjonsinntektVilkårsvurderingKategori(
    val pensjonsinntektVilkårsvurdering: PensjonsinntektVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.PENSJONSINNTEKT

    override fun samletUtfall(): Utfall =
        pensjonsinntektVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> =
        pensjonsinntektVilkårsvurdering.vurderinger()
}
