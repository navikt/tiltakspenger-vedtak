package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class Vurdering(
    val vilkår: Vilkår,
    val kilde: Kilde,
    val utfall: Periodisering<Utfall>,
    val detaljer: String,
    val grunnlagId: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vurdering

        if (vilkår != other.vilkår) return false
        if (kilde != other.kilde) return false
        if (utfall != other.utfall) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vilkår.hashCode()
        result = 31 * result + kilde.hashCode()
        result = 31 * result + utfall.hashCode()
        return result
    }
}
