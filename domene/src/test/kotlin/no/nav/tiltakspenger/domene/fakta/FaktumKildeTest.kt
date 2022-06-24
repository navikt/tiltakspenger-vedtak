package no.nav.tiltakspenger.domene.fakta

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class FaktumKildeTest {
    @Test
    fun `SAKSBEHANDLER har høyest prioritet, deretter SYSTEM og til slutt BRUKER`() {
        assertTrue(FaktumKilde.BRUKER < FaktumKilde.SYSTEM)
        assertTrue(FaktumKilde.SYSTEM < FaktumKilde.SAKSBEHANDLER)
        assertTrue(FaktumKilde.BRUKER < FaktumKilde.SAKSBEHANDLER)

    }
}
