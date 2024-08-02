package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering

interface Vilkår {
    val vurderingsperiode: Periode
    val lovreferanse: Lovreferanse

    fun utfall(): Periodisering<UtfallForPeriode>

    fun samletUtfall(): SamletUtfall {
        val utfall = utfall()
        return when {
            utfall.perioder().any { it.verdi == UtfallForPeriode.UAVKLART } -> SamletUtfall.UAVKLART
            utfall.perioder().all { it.verdi == UtfallForPeriode.OPPFYLT } -> SamletUtfall.OPPFYLT
            utfall.perioder().all { it.verdi == UtfallForPeriode.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
            utfall.perioder().any() { it.verdi == UtfallForPeriode.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
            else -> throw IllegalStateException("Ugyldig utfall")
        }
    }
}
