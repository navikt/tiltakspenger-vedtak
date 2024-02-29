package no.nav.tiltakspenger.felles.temp

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                255L,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 1)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 2)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 3)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 4), LocalDate.of(2023, 10, 4)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 5)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 6)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 7), LocalDate.of(2023, 10, 7)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 8), LocalDate.of(2023, 10, 8)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 9), LocalDate.of(2023, 10, 9)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 10), LocalDate.of(2023, 10, 10)),
                )
        perioderMedDagsats.perioder().size shouldBe 1
    }

    @Test
    fun test2() {
        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                255L,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 1)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 2)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 3)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 4), LocalDate.of(2023, 10, 4)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 5)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 6)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 7), LocalDate.of(2023, 10, 7)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 8), LocalDate.of(2023, 10, 8)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 9), LocalDate.of(2023, 10, 9)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 10), LocalDate.of(2023, 10, 10)),
                )
        perioderMedDagsats.perioder().size shouldBe 10
    }

    @Test
    fun test3() {
        val perioderMedDagsats =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                255L,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 1)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 2)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 3)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 4), LocalDate.of(2023, 10, 4)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 5)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 6)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 7), LocalDate.of(2023, 10, 7)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 8), LocalDate.of(2023, 10, 8)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 9), LocalDate.of(2023, 10, 9)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 10), LocalDate.of(2023, 10, 10)),
                )
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
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                255L,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 4)),
            )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 1)),
                )
                .erstattSubPeriodeMedVerdi(
                    300L,
                    Periode(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 2)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 3)),
                )
                .erstattSubPeriodeMedVerdi(
                    301L,
                    Periode(LocalDate.of(2023, 10, 4), LocalDate.of(2023, 10, 4)),
                )
        kontrollerDagsats(perioderMedDagsats)

        val perioderMedAntallBarn =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                0,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 4)),
            )
                .erstattSubPeriodeMedVerdi(
                    1,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 1)),
                )
                .erstattSubPeriodeMedVerdi(
                    2,
                    Periode(LocalDate.of(2023, 10, 2), LocalDate.of(2023, 10, 2)),
                )
                .erstattSubPeriodeMedVerdi(
                    2,
                    Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 3)),
                )
                .erstattSubPeriodeMedVerdi(
                    1,
                    Periode(LocalDate.of(2023, 10, 4), LocalDate.of(2023, 10, 4)),
                )
        kontrollerAntallBarn(perioderMedAntallBarn)

        val perioderMedDagsatsOgAntallBarn =
            perioderMedDagsats.kombiner(perioderMedAntallBarn, DagsatsOgAntallBarn::kombinerDagsatsOgAntallBarn)
        perioderMedDagsatsOgAntallBarn.perioder().size shouldBe 4

        kontrollerDagsats(perioderMedDagsatsOgAntallBarn.splitt(DagsatsOgAntallBarn::trekkUtDagsats))
        kontrollerAntallBarn(perioderMedDagsatsOgAntallBarn.splitt(DagsatsOgAntallBarn::trekkUtAntallBarn))
    }
}
