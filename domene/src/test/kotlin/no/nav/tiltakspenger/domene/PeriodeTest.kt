package no.nav.tiltakspenger.domene

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class PeriodeTest {
    private val periode1 = Periode(fra = 13.mai(2022), til = 18.mai(2022))
    private val periode2 = Periode(fra = 17.mai(2022), til = 21.mai(2022))
    private val periode3 = Periode(fra = 19.mai(2022), til = 20.mai(2022))

    @Test
    fun inneholderHele() {
        assertTrue(periode1.inneholderHele(periode1))
        assertTrue(periode2.inneholderHele(periode3))
        assertFalse(periode1.inneholderHele(periode2))
    }

    @Test
    fun overlapperMed() {
        assertTrue(periode1.overlapperMed(periode1))
        assertTrue(periode1.overlapperMed(periode2))
        assertFalse(periode1.overlapperMed(periode3))
    }

    @Test
    fun intersect() {
        val fellesperiode = Periode(fra = 17.mai(2022), til = 18.mai(2022))
        assertEquals(periode1, periode1.overlappendePeriode(periode1))
        assertEquals(fellesperiode, periode1.overlappendePeriode(periode2))
        assertEquals(fellesperiode, periode2.overlappendePeriode(periode1))
        assertNotEquals(fellesperiode, periode2.overlappendePeriode(periode2))
    }


    @Test
    fun overlapperIkkeMed() {
        val periodeSomIkkeOverlapper = Periode(fra = 13.mai(2022), til = 16.mai(2022))
        assertEquals(periodeSomIkkeOverlapper, periode1.ikkeOverlappendePeriode(periode2))
    }

    @Test
    fun inneholder() {
        assertTrue(periode1.contains(17.mai(2022)))
        assertFalse(periode1.contains(1.mai(2022)))
    }
}
