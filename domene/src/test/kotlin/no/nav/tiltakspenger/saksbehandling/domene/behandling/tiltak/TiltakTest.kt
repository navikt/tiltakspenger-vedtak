package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.Periode
import org.junit.jupiter.api.Test

class TiltakTest {

    @Test
    fun `filtrerTiltakOgBeregnVurderingsperiode skal fungere rekursivt`() {
        val vurderingsperiode = Periode(1.januar(2024), 10.januar(2024))

        val tiltak1 = mockk<Tiltak>()
        every { tiltak1.deltakelsePeriode() } returns Periode(1.januar(2024), 11.januar(2024))
        every { tiltak1.girRettPåTiltakspenger() } returns true
        val tiltak2 = mockk<Tiltak>()
        every { tiltak2.deltakelsePeriode() } returns Periode(11.januar(2024), 12.januar(2024))
        every { tiltak2.girRettPåTiltakspenger() } returns true
        val tiltak3 = mockk<Tiltak>()
        every { tiltak3.deltakelsePeriode() } returns Periode(12.januar(2024), 15.januar(2024))
        every { tiltak3.girRettPåTiltakspenger() } returns true
        val tiltak4 = mockk<Tiltak>()
        every { tiltak4.deltakelsePeriode() } returns Periode(16.januar(2024), 18.januar(2024))
        every { tiltak4.girRettPåTiltakspenger() } returns true
        val tiltaksliste = listOf(tiltak1, tiltak2, tiltak3, tiltak4)

        val (nyVurderingsperiode, filtrerteTiltak) = filtrerTiltakOgBeregnVurderingsperiode(
            vurderingsperiode,
            tiltaksliste,
        )
        nyVurderingsperiode shouldBe Periode(1.januar(2024), 15.januar(2024))
        filtrerteTiltak.size shouldBe 3
        filtrerteTiltak shouldContainExactly listOf(tiltak1, tiltak2, tiltak3)
    }
}
