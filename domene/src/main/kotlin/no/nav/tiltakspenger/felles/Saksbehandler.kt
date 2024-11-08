package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Roller

data class Saksbehandler(
    val navIdent: String,
    // TODO post-mvp: Dersom brukernavn og epost ikke brukes, fjerne disse fra Saksbehandler. Brukernavn utledes fra epost og det føles ikke idéelt.
    override val brukernavn: String,
    val epost: String,
    override val roller: Roller,
) : Bruker {
    fun erSaksbehandler() = roller.erSaksbehandler()

    fun erBeslutter() = roller.erBeslutter()

    fun erSaksbehandlerEllerBeslutter() = roller.erSaksbehandlerEllerBeslutter()

    override fun toString(): String =
        "Saksbehandler(navIdent='$navIdent', brukernavn='*****', epost='*****', roller=$roller)"
}
