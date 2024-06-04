package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering

data class Vurdering(
    val vilkår: Vilkår,
    val utfall: Periodisering<Utfall>,
    val detaljer: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vurdering

        if (vilkår != other.vilkår) return false
        if (utfall != other.utfall) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vilkår.hashCode()
        result = 31 * result + utfall.hashCode()
        return result
    }
}
