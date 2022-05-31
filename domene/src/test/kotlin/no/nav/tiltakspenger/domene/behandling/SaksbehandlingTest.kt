package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.KVPFaktumBruker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class SaksbehandlingTest {

    @Test
    fun `en søknad oppretter en førstegangsbehandling og starter vurdering`() {
        val saksbehandling = Førstegangsbehandling("123453553543")
        assertTrue(saksbehandling.tilstand is Førstegangsbehandling.Tilstand.Start)

        val søknad = Søknad(
            id = "1212",
            ident = "123",
            opprettet = LocalDateTime.now(),
            deltarKvp = false,
            tiltak = Tiltak("", "", "", LocalDate.now(), LocalDate.now())
        )
        saksbehandling.behandle(søknad)

        assertTrue(saksbehandling.tilstand is Førstegangsbehandling.Tilstand.Vurder)
    }

    @Test
    fun `kan motta fakta`() {
        val behandling = Førstegangsbehandling("asds")
        val vurderinger = behandling.vilkårsvurderinger.vilkårsvurderinger
            .map {
                it.vurder(KVPFaktumBruker(deltarKVP = true))
            }
    }
}
