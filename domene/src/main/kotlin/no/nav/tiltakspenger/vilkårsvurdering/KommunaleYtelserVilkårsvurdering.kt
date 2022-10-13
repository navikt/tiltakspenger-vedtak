package no.nav.tiltakspenger.vilkårsvurdering

class KommunaleYtelserVilkårsvurdering(
    private val introProgrammetVilkårsvurdering: IntroProgrammetVilkårsvurdering,
    private val kvpVilkårsvurdering: KVPVilkårsvurdering
) {
    fun samletUtfall(): Utfall {
        return if (
            introProgrammetVilkårsvurdering.samletUtfall() == Utfall.IKKE_OPPFYLT ||
            kvpVilkårsvurdering.samletUtfall() == Utfall.IKKE_OPPFYLT
        ) {
            Utfall.IKKE_OPPFYLT
        } else if (
            introProgrammetVilkårsvurdering.samletUtfall() == Utfall.KREVER_MANUELL_VURDERING ||
            kvpVilkårsvurdering.samletUtfall() == Utfall.KREVER_MANUELL_VURDERING
        ) {
            Utfall.KREVER_MANUELL_VURDERING
        } else {
            Utfall.OPPFYLT
        }
    }

}
