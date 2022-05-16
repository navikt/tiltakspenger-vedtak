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
        assertEquals(periodeSomIkkeOverlapper, periode1.ikkeOverlappendePeriode(periode2).first())
    }

    @Test
    fun `to komplett overlappende perioder skal gi tomt svar`() {
        assertEquals(emptyList<Periode>(), periode1.ikkeOverlappendePeriode(periode1))
    }

    @Test
    fun `to overlappende perioder`() {
        val periodeEn = Periode(fra = 13.mai(2022), til = 16.mai(2022))
        val periodeTo = Periode(fra = 13.mai(2022), til = 15.mai(2022))
        val fasit = Periode(fra = 16.mai(2022), til = 16.mai(2022))
        assertEquals(fasit, periodeEn.ikkeOverlappendePeriode(periodeTo).first())
        assertEquals(1, periodeEn.ikkeOverlappendePeriode(periodeTo).size)
    }
}
