package no.nav.tiltakspenger.domene

interface Faktum

class AldersFaktum(
    val alder: Int,
    val ident: String
): Faktum {
    fun erOver18(): Boolean {
        return alder > 17
    }
}