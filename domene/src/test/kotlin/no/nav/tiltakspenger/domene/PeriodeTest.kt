package no.nav.tiltakspenger.domene

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `ikkeOverlappendePerioder fjerner overlapp mellom flere perioder --fengsel---kvp---`() {
        val periodeEn = Periode(fra = 1.mai(2022), til = 15.mai(2022))
        val fengselPeriode = Periode(fra = 5.mai(2022), til = 6.mai(2022))
        val kvpPeriode = Periode(fra = 11.mai(2022), til = 12.mai(2022))

        val result = periodeEn.ikkeOverlappendePerioder(
            listOf(
                fengselPeriode,
                kvpPeriode
            )
        )
        assertEquals(3, result.size)
        assertEquals(
            listOf(
                Periode(fra = 1.mai(2022), til = 4.mai(2022)),
                Periode(fra = 7.mai(2022), til = 10.mai(2022)),
                Periode(fra = 13.mai(2022), til = 15.mai(2022))
            ),
            result
        )
    }

    @Test
    fun `ikkeOverlappendePerioder fjerner overlapp mellom flere perioder --fengselOgKvp---`() {
        val periodeEn = Periode(fra = 1.mai(2022), til = 15.mai(2022))
        val fengselPeriode = Periode(fra = 5.mai(2022), til = 11.mai(2022))
        val kvpPeriode = Periode(fra = 10.mai(2022), til = 12.mai(2022))

        val result = periodeEn.ikkeOverlappendePerioder(
            listOf(
                fengselPeriode,
                kvpPeriode
            )
        )
        assertEquals(2, result.size)
        assertEquals(
            listOf(
                Periode(fra = 1.mai(2022), til = 4.mai(2022)),
                Periode(fra = 13.mai(2022), til = 15.mai(2022))
            ),
            result
        )
    }

    @Test
    fun `man kan trekke en periode fra en annen periode`() {
        val periodeEn = Periode(fra = 3.mai(2022), til = 15.mai(2022))
        val periodeTo = Periode(fra = 6.mai(2022), til = 12.mai(2022))
        val perioder = periodeEn.trekkFra(listOf(periodeTo))
        assertEquals(2, perioder.size)
        assertEquals(3.mai(2022), perioder[0].fra)
        assertEquals(5.mai(2022), perioder[0].til)
        assertEquals(13.mai(2022), perioder[1].fra)
        assertEquals(15.mai(2022), perioder[1].til)
    }

    @Test
    fun `man kan trekke en periode fra en annen ikke-overlappende periode`() {
        val periodeEn = Periode(fra = 3.mai(2022), til = 15.mai(2022))
        val periodeTo = Periode(fra = 6.mai(2022), til = 18.mai(2022))
        val perioder = periodeEn.trekkFra(listOf(periodeTo))
        assertEquals(1, perioder.size)
        assertEquals(3.mai(2022), perioder[0].fra)
        assertEquals(5.mai(2022), perioder[0].til)
    }

    @Test
    fun `man kan trekke flere perioder fra en annen periode`() {
        val periodeEn = Periode(fra = 3.mai(2022), til = 15.mai(2022))
        val periodeTo = Periode(fra = 6.mai(2022), til = 8.mai(2022))
        val periodeTre = Periode(fra = 10.mai(2022), til = 12.mai(2022))
        val perioder = periodeEn.trekkFra(listOf(periodeTo, periodeTre))
        assertEquals(3, perioder.size)
        assertEquals(3.mai(2022), perioder[0].fra)
        assertEquals(5.mai(2022), perioder[0].til)
        assertEquals(9.mai(2022), perioder[1].fra)
        assertEquals(9.mai(2022), perioder[1].til)
        assertEquals(13.mai(2022), perioder[2].fra)
        assertEquals(15.mai(2022), perioder[2].til)
    }

    @Test
    fun `man kan trekke flere connected perioder fra en annen periode`() {
        val periodeEn = Periode(fra = 3.mai(2022), til = 15.mai(2022))
        val periodeTo = Periode(fra = 6.mai(2022), til = 9.mai(2022))
        val periodeTre = Periode(fra = 10.mai(2022), til = 12.mai(2022))
        val perioder = periodeEn.trekkFra(listOf(periodeTo, periodeTre))
        assertEquals(2, perioder.size)
        assertEquals(3.mai(2022), perioder[0].fra)
        assertEquals(5.mai(2022), perioder[0].til)
        assertEquals(13.mai(2022), perioder[1].fra)
        assertEquals(15.mai(2022), perioder[1].til)
    }

    @Test
    fun `man kan ikke trekke flere overlappende perioder fra en annen periode`() {
        val periodeEn = Periode(fra = 3.mai(2022), til = 15.mai(2022))
        val periodeTo = Periode(fra = 6.mai(2022), til = 10.mai(2022))
        val periodeTre = Periode(fra = 9.mai(2022), til = 12.mai(2022))
        assertThrows(IllegalArgumentException::class.java) {
            periodeEn.trekkFra(listOf(periodeTo, periodeTre))
        }
    }
}
