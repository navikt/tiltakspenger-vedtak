package no.nav.tiltakspenger.felles

import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller

data class Saksbehandler(
    val navIdent: String,
    override val brukernavn: String,
    val epost: String,
    override val roller: Roller,
) : Bruker {
    fun isSaksbehandler() = roller.harRolle(Rolle.SAKSBEHANDLER)

    fun isBeslutter() = roller.contains(Rolle.BESLUTTER)

    override fun toString(): String = "Saksbehandler(navIdent='$navIdent', brukernavn='*****', epost='*****', roller=$roller)"
}
