package no.nav.tiltakspenger.saksbehandling.domene.vedtak

data class Vedtaksliste(
    val value: List<Vedtak>,
) : List<Vedtak> by value {
    init {
        value.map { it.id }.let {
            require(it.size == it.distinct().size) { "Vedtakene må ha unike IDer men var: $it" }
        }
    }
}
