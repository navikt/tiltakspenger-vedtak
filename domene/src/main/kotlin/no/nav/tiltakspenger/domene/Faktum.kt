package no.nav.tiltakspenger.domene

enum class FaktumKilde {
    BRUKER,
    SYSTEM,
    SAKSBEHANDLER
}

interface Faktum {
    val kilde: FaktumKilde
}

class AldersFaktum(
    val alder: Int,
    val ident: String,
    override val kilde: FaktumKilde
): Faktum {
    fun erOver18(): Boolean {
        return alder > 17
    }
}