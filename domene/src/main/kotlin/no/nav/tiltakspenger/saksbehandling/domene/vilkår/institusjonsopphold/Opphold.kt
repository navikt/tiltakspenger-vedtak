package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode

enum class Opphold {
    OPPHOLD,
    IKKE_OPPHOLD,
    ;

    fun vurderMaskinelt(): UtfallForPeriode {
        return when (this) {
            OPPHOLD -> UtfallForPeriode.IKKE_OPPFYLT
            IKKE_OPPHOLD -> UtfallForPeriode.OPPFYLT
        }
    }
}
