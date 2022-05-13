package no.nav.tiltakspenger.domene

data class KVPFaktum(val deltarKVP: Boolean, override val kilde: FaktumKilde) : Faktum
