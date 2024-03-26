package no.nav.tiltakspenger.felles

data class Saksbehandler(
    val navIdent: String,
    override val brukernavn: String,
    val epost: String,
    override val roller: List<Rolle>,
) : Bruker {
    override val ident
        get() = navIdent
}
