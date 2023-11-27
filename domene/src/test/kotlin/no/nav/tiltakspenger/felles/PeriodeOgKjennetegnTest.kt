package no.nav.tiltakspenger.felles

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PeriodeOgKjennetegnTest {

    data class TestKjennetegn(val string1: String, val int1: Int) : Kjennetegn
    data class TestPeriodeOgKjennetegn(
        val periode: Periode,
        val kjennetegn: Kjennetegn,
    ) : PeriodeOgKjennetegn {
        override fun kjennetegn(): Kjennetegn = kjennetegn
        override fun periode(): Periode = periode
    }

    @Test
    fun `to perioder med ulike kjennetegn blir skilt`() {
        val periodeEn = Periode(fra = 6.mai(2022), til = 15.mai(2022))
        val kjennetegnEn = TestKjennetegn("foo", 1)
        val periodeOgKjennetegnEn = TestPeriodeOgKjennetegn(periodeEn, kjennetegnEn)
        val periodeTo = Periode(fra = 1.mai(2022), til = 5.mai(2022))
        val kjennetegnTo = TestKjennetegn("foo", 2)
        val periodeOgKjennetegnTo = TestPeriodeOgKjennetegn(periodeTo, kjennetegnTo)

        assertEquals(
            2,
            listOf(periodeOgKjennetegnEn, periodeOgKjennetegnTo).sammenhengendePerioderPerKjennetegn().size,
        )
    }

    @Test
    fun `to perioder med like kjennetegn blir samlet`() {
        val periodeEn = Periode(fra = 6.mai(2022), til = 15.mai(2022))
        val kjennetegnEn = TestKjennetegn("foo", 1)
        val periodeOgKjennetegnEn = TestPeriodeOgKjennetegn(periodeEn, kjennetegnEn)
        val periodeTo = Periode(fra = 1.mai(2022), til = 5.mai(2022))
        val kjennetegnTo = TestKjennetegn("foo", 1)
        val periodeOgKjennetegnTo = TestPeriodeOgKjennetegn(periodeTo, kjennetegnTo)

        assertEquals(
            1,
            listOf(periodeOgKjennetegnEn, periodeOgKjennetegnTo).sammenhengendePerioderPerKjennetegn().size,
        )
    }
}
