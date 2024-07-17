package no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

enum class HarYtelse {
    HAR_YTELSE,
    HAR_IKKE_YTELSE,
    ;

    fun vurderMaskinelt(): Utfall2 {
        return when (this) {
            HAR_YTELSE -> Utfall2.IKKE_OPPFYLT
            HAR_IKKE_YTELSE -> Utfall2.OPPFYLT
        }
    }
}
