package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

enum class Opphold {
    OPPHOLD,
    IKKE_OPPHOLD,
    ;

    fun vurderMaskinelt(): Utfall2 {
        return when (this) {
            OPPHOLD -> Utfall2.IKKE_OPPFYLT
            IKKE_OPPHOLD -> Utfall2.OPPFYLT
        }
    }
}
