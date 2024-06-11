package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering

data class LivsoppholdDelVurdering(
    val delVilkår: LivsoppholdDelVilkår,
    val utfall: Periodisering<Utfall>,
    val detaljer: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LivsoppholdDelVurdering

        if (delVilkår != other.delVilkår) return false
        if (utfall != other.utfall) return false

        return true
    }

    override fun hashCode(): Int {
        var result = delVilkår.hashCode()
        result = 31 * result + utfall.hashCode()
        return result
    }
}
