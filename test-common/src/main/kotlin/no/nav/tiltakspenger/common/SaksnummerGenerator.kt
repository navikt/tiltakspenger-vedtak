package no.nav.tiltakspenger.common

import arrow.atomic.Atomic
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer

/**
 * Trådsikker. Dersom tester deler database, bør de bruke en felles statisk versjon av denne.
 */
class SaksnummerGenerator(
    private val første: Saksnummer = Saksnummer.genererSaknummer(),
) {
    private val neste = Atomic(første)

    fun neste(): String {
        return neste.getAndUpdate { it.nesteSaksnummer() }.toString()
    }
}
