package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering

data class Vurdering(
    val utfall: Periodisering<Utfall>,
    val detaljer: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vurdering

        return utfall == other.utfall
    }

    override fun hashCode(): Int {
        return utfall.hashCode()
    }
}
