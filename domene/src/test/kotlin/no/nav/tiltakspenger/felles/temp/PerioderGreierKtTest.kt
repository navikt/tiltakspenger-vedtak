package no.nav.tiltakspenger.felles.temp

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.oktober
import org.junit.jupiter.api.Test

class PerioderGreierKtTest {
    data class DagsatsOgAntallBarn(
        val dagsats: Long,
        val antallBarn: Int,
    ) {
        companion object {
            fun kombinerDagsatsOgAntallBarn(dagsats: Long, antallBarn: Int): DagsatsOgAntallBarn =
                DagsatsOgAntallBarn(dagsats, antallBarn)

            fun trekkUtDagsats(dagsatsOgAntallBarn: DagsatsOgAntallBarn): Long = dagsatsOgAntallBarn.dagsats
            fun trekkUtAntallBarn(dagsatsOgAntallBarn: DagsatsOgAntallBarn): Int = dagsatsOgAntallBarn.antallBarn
        }
    }

    @Test
    fun test1() {
        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(255L, Periode(1.oktober(2023), 10.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(1.oktober(2023), 1.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(2.oktober(2023), 2.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(3.oktober(2023), 3.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(4.oktober(2023), 4.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(5.oktober(2023), 5.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(6.oktober(2023), 6.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(7.oktober(2023), 7.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(8.oktober(2023), 8.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(9.oktober(2023), 9.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(10.oktober(2023), 10.oktober(2023)))
        perioderMedDagsats.perioder().size shouldBe 1
    }

    @Test
    fun test2() {
        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(255L, Periode(1.oktober(2023), 10.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(1.oktober(2023), 1.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(2.oktober(2023), 2.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(3.oktober(2023), 3.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(4.oktober(2023), 4.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(5.oktober(2023), 5.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(6.oktober(2023), 6.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(7.oktober(2023), 7.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(8.oktober(2023), 8.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(9.oktober(2023), 9.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(10.oktober(2023), 10.oktober(2023)))
        perioderMedDagsats.perioder().size shouldBe 10
    }

    @Test
    fun test3() {
        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(255L, Periode(1.oktober(2023), 10.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(1.oktober(2023), 1.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(2.oktober(2023), 2.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(3.oktober(2023), 3.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(4.oktober(2023), 4.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(5.oktober(2023), 5.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(6.oktober(2023), 6.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(7.oktober(2023), 7.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(8.oktober(2023), 8.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(9.oktober(2023), 9.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(10.oktober(2023), 10.oktober(2023)))
        perioderMedDagsats.perioder().size shouldBe 5
        perioderMedDagsats.perioder().count { it.verdi == 300L } shouldBe 3
        perioderMedDagsats.perioder().count { it.verdi == 301L } shouldBe 2
        perioderMedDagsats.perioder().count { it.verdi == 255L } shouldBe 0
    }

    @Test
    fun test4() {
        fun kontrollerDagsats(perioderMedDagsats: PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<Long>) {
            perioderMedDagsats.perioder().size shouldBe 2
            perioderMedDagsats.perioder().count { it.verdi == 300L } shouldBe 1
            perioderMedDagsats.perioder().count { it.verdi == 301L } shouldBe 1
            perioderMedDagsats.perioder().count { it.verdi == 255L } shouldBe 0
        }

        fun kontrollerAntallBarn(perioderMedAntallBarn: PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier<Int>) {
            perioderMedAntallBarn.perioder().size shouldBe 3
            perioderMedAntallBarn.perioder().count { it.verdi == 1 } shouldBe 2
            perioderMedAntallBarn.perioder().count { it.verdi == 2 } shouldBe 1
            perioderMedAntallBarn.perioder().count { it.verdi == 0 } shouldBe 0
        }

        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(255L, Periode(1.oktober(2023), 4.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(1.oktober(2023), 1.oktober(2023)))
                .erstattSubPeriodeMedVerdi(300L, Periode(2.oktober(2023), 2.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(3.oktober(2023), 3.oktober(2023)))
                .erstattSubPeriodeMedVerdi(301L, Periode(4.oktober(2023), 4.oktober(2023)))
        kontrollerDagsats(perioderMedDagsats)

        val perioderMedAntallBarn =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(0, Periode(1.oktober(2023), 4.oktober(2023)))
                .erstattSubPeriodeMedVerdi(1, Periode(1.oktober(2023), 1.oktober(2023)))
                .erstattSubPeriodeMedVerdi(2, Periode(2.oktober(2023), 2.oktober(2023)))
                .erstattSubPeriodeMedVerdi(2, Periode(3.oktober(2023), 3.oktober(2023)))
                .erstattSubPeriodeMedVerdi(1, Periode(4.oktober(2023), 4.oktober(2023)))
        kontrollerAntallBarn(perioderMedAntallBarn)

        val perioderMedDagsatsOgAntallBarn =
            perioderMedDagsats.kombiner(perioderMedAntallBarn, DagsatsOgAntallBarn::kombinerDagsatsOgAntallBarn)
        perioderMedDagsatsOgAntallBarn.perioder().size shouldBe 4

        kontrollerDagsats(perioderMedDagsatsOgAntallBarn.splitt(DagsatsOgAntallBarn::trekkUtDagsats))
        kontrollerAntallBarn(perioderMedDagsatsOgAntallBarn.splitt(DagsatsOgAntallBarn::trekkUtAntallBarn))
    }
}
