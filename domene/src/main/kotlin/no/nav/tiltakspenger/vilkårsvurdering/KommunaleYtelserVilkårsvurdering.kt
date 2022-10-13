package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT

class KommunaleYtelserVilkårsvurdering(
    private val introProgrammetVilkårsvurdering: IntroProgrammetVilkårsvurdering,
    private val kvpVilkårsvurdering: KVPVilkårsvurdering
) {
    fun samletUtfall(): Utfall {
        return when {
            introProgrammetVilkårsvurdering.samletUtfall() == IKKE_OPPFYLT ||
                    kvpVilkårsvurdering.samletUtfall() == IKKE_OPPFYLT -> IKKE_OPPFYLT

            introProgrammetVilkårsvurdering.samletUtfall() == KREVER_MANUELL_VURDERING ||
                    kvpVilkårsvurdering.samletUtfall() == KREVER_MANUELL_VURDERING -> KREVER_MANUELL_VURDERING

            else -> OPPFYLT
        }
    }
}
