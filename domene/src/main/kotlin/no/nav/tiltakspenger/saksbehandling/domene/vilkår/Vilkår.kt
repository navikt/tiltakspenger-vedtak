package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering

interface Vilkår {
    val vurderingsperiode: Periode
    val lovreferanse: Lovreferanse

    val utfall: Periodisering<UtfallForPeriode>

    fun samletUtfall(): SamletUtfall {
        return when {
            utfall.any { it.verdi == UtfallForPeriode.UAVKLART } -> SamletUtfall.UAVKLART
            utfall.all { it.verdi == UtfallForPeriode.OPPFYLT } -> SamletUtfall.OPPFYLT
            utfall.all { it.verdi == UtfallForPeriode.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
            utfall.any { it.verdi == UtfallForPeriode.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
            else -> throw IllegalStateException("Ugyldig utfall")
        }
    }

    fun krymp(nyPeriode: Periode): Vilkår
}
