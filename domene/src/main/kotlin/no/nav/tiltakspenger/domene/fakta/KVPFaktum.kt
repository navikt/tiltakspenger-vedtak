package no.nav.tiltakspenger.domene.fakta

data class KVPFakta (
    val bruker: KVPFaktumBruker? = null,
    val system: KVPFaktumSystem? = null,
    val saksbehandler: KVPFaktumSaksbehandler? = null,
): Fakta<KVPFaktum> {
    override fun leggTil(faktum: KVPFaktum): Fakta<KVPFaktum> {
        return when(faktum) {
            is KVPFaktumBruker -> this.copy(bruker = faktum)
            is KVPFaktumSystem -> this.copy(system = faktum)
            is KVPFaktumSaksbehandler -> this.copy(saksbehandler = faktum)
            else -> throw IllegalArgumentException("Unexpected instance of KVPEnkeltFaktum")
        }
    }
}

interface KVPFaktum: Faktum

data class KVPFaktumBruker(
    val deltarKVP: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.BRUKER,
): KVPFaktum
data class KVPFaktumSystem(
    val deltarKVP: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.SYSTEM,
): KVPFaktum
data class KVPFaktumSaksbehandler(
    val deltarKVP: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.SAKSBEHANDLER,
): KVPFaktum
