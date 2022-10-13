package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.vilkårsvurdering.Utfall.IKKE_OPPFYLT
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.KREVER_MANUELL_VURDERING
import no.nav.tiltakspenger.vilkårsvurdering.Utfall.OPPFYLT

class KommunaleYtelserVilkårsvurdering(
    private val intro: IntroProgrammetVilkårsvurdering,
    private val kvp: KVPVilkårsvurdering
) {
    fun samletUtfall(): Utfall {
        return when {
            intro.samletUtfall() == IKKE_OPPFYLT || kvp.samletUtfall() == IKKE_OPPFYLT -> IKKE_OPPFYLT

            intro.samletUtfall() == KREVER_MANUELL_VURDERING ||
                    kvp.samletUtfall() == KREVER_MANUELL_VURDERING -> KREVER_MANUELL_VURDERING

            else -> OPPFYLT
        }
    }
}
