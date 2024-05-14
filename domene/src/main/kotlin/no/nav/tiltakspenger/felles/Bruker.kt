package no.nav.tiltakspenger.felles

sealed interface Bruker {
    val brukernavn: String
    val roller: List<Rolle>
    val ident: String

    fun isAdmin() = roller.contains(Rolle.ADMINISTRATOR)
    fun isSaksbehandler() = roller.contains(Rolle.SAKSBEHANDLER)
    fun isBeslutter() = roller.contains(Rolle.BESLUTTER)
}
