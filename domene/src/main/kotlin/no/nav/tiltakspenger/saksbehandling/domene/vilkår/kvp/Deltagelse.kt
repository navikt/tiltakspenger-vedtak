package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

enum class Deltagelse {
    DELTAR,
    DELTAR_IKKE,
    ;

    fun vurderMaskinelt(): Utfall2 {
        return when (this) {
            DELTAR -> Utfall2.IKKE_OPPFYLT
            DELTAR_IKKE -> Utfall2.OPPFYLT
        }
    }
}
