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

data class Vilkår(val utfall: Utfall)

class VilkårsvurderingPeriodiseringTest {
    @Test
    fun test1() {
        val aap =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.IKKE_OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 10)),
                )

        val dagpenger =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )

        val vedtak = aap.kombiner(dagpenger, ::kombinerToUfall)
        vedtak.perioderMedUlikVerdi.size shouldBe 2

        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.OPPFYLT }!!.perioder.perioder().size shouldBe 1
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.OPPFYLT }!!.perioder.perioder().first().fra shouldBe LocalDate.of(2023, 10, 6)
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.OPPFYLT }!!.perioder.perioder().first().til shouldBe LocalDate.of(2023, 10, 10)

        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.perioder.perioder().size shouldBe 1
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.perioder.perioder().first().fra shouldBe LocalDate.of(
            2023,
            10,
            1,
        )
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.perioder.perioder().first().til shouldBe LocalDate.of(
            2023,
            10,
            5,
        )
    }

    @Test
    fun testFireVilkår() {
        val aap =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 6), LocalDate.of(2023, 10, 10)),
                )

        val fengsel =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 2)),
                )

        val jobbsjansen =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )
                .erstattSubPeriodeMedVerdi(
                    Utfall.IKKE_OPPFYLT,
                    Periode(LocalDate.of(2023, 10, 5), LocalDate.of(2023, 10, 7)),
                )

        val dagpenger =
            IkkeOverlappendePerioderMedUlikeVerdier(
                Utfall.OPPFYLT,
                Periode(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10)),
            )

        val alleVilkår = listOf(aap, dagpenger, fengsel, jobbsjansen)

        val vedtak = IkkeOverlappendePerioderMedUlikeVerdier.kombinerLike(alleVilkår, ::kombinerToUfall)
        println(vedtak)
        vedtak.perioderMedUlikVerdi.size shouldBe 2

        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.OPPFYLT }!!.perioder.perioder().size shouldBe 1
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.OPPFYLT }!!.perioder.perioder() shouldContainExactly listOf(
            Periode(LocalDate.of(2023, 10, 3), LocalDate.of(2023, 10, 4)),
        )

        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.perioder.perioder().size shouldBe 2
        vedtak.perioderMedUlikVerdi
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.perioder.perioder() shouldContainExactly listOf(
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
