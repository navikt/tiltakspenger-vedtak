package no.nav.tiltakspenger.common

import java.util.concurrent.atomic.AtomicLong

/**
 * Trådsikker. Dersom tester deler database, bør de bruke en felles statisk versjon av denne.
 */
class JournalpostIdGenerator(
    private val første: Long,
) {
    private val neste = AtomicLong(første)

    fun neste(): String {
        return neste.getAndIncrement().toString()
    }
}
