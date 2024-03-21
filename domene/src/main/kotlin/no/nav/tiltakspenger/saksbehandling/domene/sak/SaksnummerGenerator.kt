package no.nav.tiltakspenger.saksbehandling.domene.sak

import java.time.LocalDate

class SaksnummerGenerator {
    fun genererSaknummer(saksnummer: String): Saksnummer =
        Saksnummer(
            LocalDate.now().let {
                it.year.toString() + String.format("%02d%02d", it.monthValue, it.dayOfMonth) + saksnummer
            },
        )
}
