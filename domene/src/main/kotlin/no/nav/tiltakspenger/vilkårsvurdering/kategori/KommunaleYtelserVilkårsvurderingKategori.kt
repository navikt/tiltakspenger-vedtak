package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

class KommunaleYtelserVilkårsvurderingKategori(
    val intro: IntroProgrammetVilkårsvurdering,
    val kvp: KVPVilkårsvurdering
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.KOMMUNALE_YTELSER

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
