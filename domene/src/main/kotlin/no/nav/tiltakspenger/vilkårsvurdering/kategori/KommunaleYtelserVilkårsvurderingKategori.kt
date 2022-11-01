package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering

class KommunaleYtelserVilkårsvurderingKategori(
    val intro: IntroProgrammetVilkårsvurdering,
    val kvp: KVPVilkårsvurdering
) : VilkårsvurderingKategori {
    override fun vilkår(): Vilkår = Vilkår.KOMMUNALEYTELSER

    override fun samletUtfall(): Utfall {
        val kommunaleUtfall = listOf(intro.samletUtfall(), kvp.samletUtfall())
        return when {
            kommunaleUtfall.any { it == IKKE_OPPFYLT } -> IKKE_OPPFYLT
            kommunaleUtfall.any { it == KREVER_MANUELL_VURDERING } -> KREVER_MANUELL_VURDERING
            else -> OPPFYLT
        }
    }

    override fun vurderinger(): List<Vurdering> =
        listOf(intro.vurderinger(), kvp.vurderinger()).flatten()
}
