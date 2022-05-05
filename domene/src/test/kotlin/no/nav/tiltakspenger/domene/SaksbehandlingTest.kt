package no.nav.tiltakspenger.domene

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SaksbehandlingTest {

    @Test
    fun `en søknad oppretter en førstegangsbehandling og starter vurdering`() {
        val saksbehandling = Førstegangsbehandling("123453553543")
        assertTrue(saksbehandling.tilstand is Førstegangsbehandling.Tilstand.Start)

        val søknad = Søknad(
            id = "1212",
            innsendtdato = LocalDate.now(),
            deltarKvp = false
        )
        saksbehandling.behandle(søknad)

        assertTrue(saksbehandling.tilstand is Førstegangsbehandling.Tilstand.Vurder)
    }
}
