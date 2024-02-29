package no.nav.tiltakspenger.felles.temp

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate

enum class Utfall {
    OPPFYLT,
    IKKE_OPPFYLT,
    KREVER_MANUELL_VURDERING,
}

class VilkårsvurderingPeriodiseringTest {
    @Test
    fun test1() {
        val aap =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.IKKE_OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 10)),
                )

        val dagpenger =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )

        val vedtak = aap.kombiner(dagpenger, ::kombinerToUfall)
        vedtak.perioder().size shouldBe 2

        vedtak.perioder().count { it.verdi == Utfall.OPPFYLT } shouldBe 1
        vedtak.perioder()
            .find { it.verdi == Utfall.OPPFYLT }!!.periode shouldBe Periode(
            LocalDate.of(2023, 10, 6),
            LocalDate.of(2023, 10, 10),
        )

        vedtak.perioder().count { it.verdi == Utfall.IKKE_OPPFYLT } shouldBe 1
        vedtak.perioder()
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.periode shouldBe Periode(
            LocalDate.of(2023, 10, 1),
            LocalDate.of(2023, 10, 5),
        )
    }

    @Test
    fun testFireVilkår() {
        val aap =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 10)),
                )

        val fengsel =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 2)),
                )

        val jobbsjansen =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 7)),
                )

        val dagpenger =
            PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )

        val alleVilkår = listOf(aap, dagpenger, fengsel, jobbsjansen)

        val vedtak = PeriodeMedIkkeOverlappendeSubPerioderMedUlikeVerdier.kombinerLike(alleVilkår, ::kombinerToUfall)
        println(vedtak)
        vedtak.perioder().size shouldBe 3

        vedtak.perioder()
            .count { it.verdi == Utfall.OPPFYLT } shouldBe 1
        vedtak.perioder()
            .filter { it.verdi == Utfall.OPPFYLT }.map { it.periode } shouldContainExactly listOf(
            Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 4)),
        )

        vedtak.perioder()
            .count { it.verdi == Utfall.IKKE_OPPFYLT } shouldBe 2
        vedtak.perioder()
            .filter { it.verdi == Utfall.IKKE_OPPFYLT }.map { it.periode } shouldContainExactly listOf(
            Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 2)),
            Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 10)),
        )
    }

    fun kombinerToUfall(en: Utfall, to: Utfall): Utfall {
        if (en == Utfall.KREVER_MANUELL_VURDERING || to == Utfall.KREVER_MANUELL_VURDERING) {
            return Utfall.KREVER_MANUELL_VURDERING
        }
        if (en == Utfall.IKKE_OPPFYLT || to == Utfall.IKKE_OPPFYLT) {
            return Utfall.IKKE_OPPFYLT
        }
        return Utfall.OPPFYLT
    }
}
