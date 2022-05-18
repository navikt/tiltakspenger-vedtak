package no.nav.tiltakspenger.domene.fakta

data class KVPFaktum(
    val deltarKVP: Boolean,
    override val kilde: FaktumKilde
) : Faktum
