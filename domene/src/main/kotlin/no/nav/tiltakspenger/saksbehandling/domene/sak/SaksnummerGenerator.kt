package no.nav.tiltakspenger.saksbehandling.domene.sak

class SaksnummerGenerator {
    fun genererSaknummer(saksnummer: String): Saksnummer =
        Saksnummer(saksnummer)
}
