package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT

class StatligeYtelserVilkårsvurderinger(
    val aap: AAPVilkårsvurdering,
    val dagpenger: DagpengerVilkårsvurdering,
    val sykepenger: SykepengerVilkårsvurdering = SykepengerVilkårsvurdering(),
) : VilkårsvurderingKategori {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.STATLIGE_YTELSER

    override fun samletUtfall(): Utfall {
        val utfall = listOf(aap.samletUtfall(), dagpenger.samletUtfall(), sykepenger.samletUtfall())
        return when {
            utfall.any { it == IKKE_OPPFYLT } -> IKKE_OPPFYLT
            utfall.any { it == KREVER_MANUELL_VURDERING } -> KREVER_MANUELL_VURDERING
            else -> OPPFYLT
        }
    }

    override fun vurderinger(): List<Vurdering> =
        listOf(aap.vurderinger(), dagpenger.vurderinger(), sykepenger.vurderinger()).flatten()
}
