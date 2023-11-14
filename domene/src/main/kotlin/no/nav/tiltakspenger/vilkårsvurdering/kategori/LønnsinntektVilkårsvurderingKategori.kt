package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.EtterlønnVilkårsvurdering

class LønnsinntektVilkårsvurderingKategori(
    val etterlønnVilkårsvurdering: EtterlønnVilkårsvurdering,
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.LØNNSINNTEKT

    override fun samletUtfall(): Utfall {
        val lønnsinntektUtfall = listOf(
            etterlønnVilkårsvurdering.samletUtfall(),
        )
        return when {
            lønnsinntektUtfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            lønnsinntektUtfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    override fun vurderinger(): List<Vurdering> =
        listOf(
            etterlønnVilkårsvurdering.vurderinger(),
        ).flatten()
}
