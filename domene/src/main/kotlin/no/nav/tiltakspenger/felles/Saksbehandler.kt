package no.nav.tiltakspenger.felles

data class Saksbehandler(
    val navIdent: String,
    override val brukernavn: String,
    val epost: String,
    override val roller: List<Rolle>,
) : Bruker {

    fun isAdmin() = roller.contains(Rolle.ADMINISTRATOR)
    fun isSaksbehandler() = roller.contains(Rolle.SAKSBEHANDLER)
    fun isBeslutter() = roller.contains(Rolle.BESLUTTER)

    override fun toString(): String {
        return "Saksbehandler(navIdent='$navIdent', brukernavn='*****', epost='*****', roller=$roller)"
    }
}
