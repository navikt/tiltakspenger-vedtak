package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering

/**
 * TODO jah: Skal erstatte Vilkår.kt. Men vi tar det iterativt.
 */
interface SkalErstatteVilkår {
    val lovreferanse: Lovreferanse

    fun utfall(): Periodisering<Utfall2>

    fun samletUtfall(): SamletUtfall {
        val utfall = utfall()
        return when {
            utfall.perioder().any { it.verdi == Utfall2.UAVKLART } -> SamletUtfall.UAVKLART
            utfall.perioder().all { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.OPPFYLT
            utfall.perioder().all { it.verdi == Utfall2.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
            utfall.perioder().any() { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
            else -> throw IllegalStateException("Ugyldig utfall")
        }
    }
}
