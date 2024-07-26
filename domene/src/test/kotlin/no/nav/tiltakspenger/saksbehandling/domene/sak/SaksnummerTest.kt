package no.nav.tiltakspenger.saksbehandling.domene.sak

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SaksnummerTest {
    @Test
    fun prefiks() {
        val saksnummer = Saksnummer.genererSaknummer(dato = LocalDate.of(2021, 1, 1), løpenr = 1001)
        saksnummer.løpenr shouldBe 1001
        saksnummer.prefiks shouldBe "20210101"
        saksnummer.dato shouldBe LocalDate.of(2021, 1, 1)
    }
}
