package no.nav.tiltakspenger.felles.temp

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.oktober
import org.junit.jupiter.api.Test

enum class Utfall {
    OPPFYLT,
    IKKE_OPPFYLT,
    KREVER_MANUELL_VURDERING,
}

class VilkårsvurderingPeriodiseringTest {
    @Test
    fun test1() {
        val aap =
            PeriodeMedVerdier(
                Utfall.IKKE_OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )
                .setDelPeriodeMedVerdi(Utfall.OPPFYLT, Periode(6.oktober(2023), 10.oktober(2023)))

        val dagpenger =
            PeriodeMedVerdier(
                Utfall.OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )

        val vedtak = aap.kombiner(dagpenger, ::kombinerToUfall)
        vedtak.perioder().size shouldBe 2

        vedtak.perioder().count { it.verdi == Utfall.OPPFYLT } shouldBe 1
        vedtak.perioder()
            .find { it.verdi == Utfall.OPPFYLT }!!.periode shouldBe Periode(6.oktober(2023), 10.oktober(2023))

        vedtak.perioder().count { it.verdi == Utfall.IKKE_OPPFYLT } shouldBe 1
        vedtak.perioder()
            .find { it.verdi == Utfall.IKKE_OPPFYLT }!!.periode shouldBe Periode(1.oktober(2023), 5.oktober(2023))
    }

    @Test
    fun testFireVilkår() {
        val aap =
            PeriodeMedVerdier(
                Utfall.OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )
                .setDelPeriodeMedVerdi(Utfall.IKKE_OPPFYLT, Periode(6.oktober(2023), 10.oktober(2023)))

        val fengsel =
            PeriodeMedVerdier(
                Utfall.OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )
                .setDelPeriodeMedVerdi(Utfall.IKKE_OPPFYLT, Periode(1.oktober(2023), 2.oktober(2023)))

        val jobbsjansen =
            PeriodeMedVerdier(
                Utfall.OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )
                .setDelPeriodeMedVerdi(Utfall.IKKE_OPPFYLT, Periode(5.oktober(2023), 7.oktober(2023)))

        val dagpenger =
            PeriodeMedVerdier(
                Utfall.OPPFYLT,
                Periode(1.oktober(2023), 10.oktober(2023)),
            )

        val alleVilkår = listOf(aap, dagpenger, fengsel, jobbsjansen)

        val vedtak = PeriodeMedVerdier.kombinerLike(alleVilkår, ::kombinerToUfall)
        println(vedtak)
        vedtak.perioder().size shouldBe 3

        vedtak.perioder()
            .count { it.verdi == Utfall.OPPFYLT } shouldBe 1
        vedtak.perioder()
            .filter { it.verdi == Utfall.OPPFYLT }
            .map { it.periode } shouldContainExactly listOf(Periode(3.oktober(2023), 4.oktober(2023)))

        vedtak.perioder()
            .count { it.verdi == Utfall.IKKE_OPPFYLT } shouldBe 2
        vedtak.perioder()
            .filter { it.verdi == Utfall.IKKE_OPPFYLT }.map { it.periode } shouldContainExactly listOf(
            Periode(1.oktober(2023), 2.oktober(2023)),
            Periode(5.oktober(2023), 10.oktober(2023)),
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
