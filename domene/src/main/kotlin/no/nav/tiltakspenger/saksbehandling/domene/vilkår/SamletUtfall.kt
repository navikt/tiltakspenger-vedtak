package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import java.lang.IllegalStateException

/**
 * Det samlede utfallet for et vilkår og vilkårsettet.
 * Forskjellen fra [UtfallForPeriode] er at her kan vi og ha [SamletUtfall.DELVIS_OPPFYLT].
 */
enum class SamletUtfall {
    OPPFYLT,
    DELVIS_OPPFYLT,
    IKKE_OPPFYLT,
    UAVKLART, ;

    /**
     * @throws IllegalStateException dersom [SamletUtfall] er [UAVKLART]
     */
    fun Periodisering<SamletUtfall>.toAvklartSamletUtfall(): Periodisering<AvklartSamletUtfall> {
        return map { it.toAvklartSamletUtfall() }
    }

    /**
     * @throws IllegalStateException dersom [SamletUtfall] er [UAVKLART]
     */
    fun toAvklartSamletUtfall(): AvklartSamletUtfall {
        return when (this) {
            OPPFYLT -> AvklartSamletUtfall.OPPFYLT
            DELVIS_OPPFYLT -> AvklartSamletUtfall.DELVIS_OPPFYLT
            IKKE_OPPFYLT -> AvklartSamletUtfall.IKKE_OPPFYLT
            UAVKLART -> throw IllegalStateException("Kan ikke konvertere UAVKLART til AvklartSamletUtfall")
        }
    }
}
