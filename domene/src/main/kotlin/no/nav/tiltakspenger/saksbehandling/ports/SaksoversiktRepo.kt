package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt

/**
 * Custom spørringer for å vise en oversikt over søknader og behandlinger.
 */
interface SaksoversiktRepo {
    fun hentAlle(sessionContext: SessionContext? = null): Saksoversikt
}
