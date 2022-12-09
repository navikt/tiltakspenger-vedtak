package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class KonklusjonTest {
    
    @Test
    fun `Skal få oppfylt når alle vurderinger er oppfylt`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
        val vilkår: List<Vilkår> = listOf(
            Vilkår.DAGPENGER,
            Vilkår.AAP,
            Vilkår.ALDERSPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.OMSORGSPENGER,
            Vilkår.SYKEPENGER,
            Vilkår.KVP,
            Vilkår.INTROPROGRAMMET
        )

        vilkår
            .map { Vurdering(it, "", null, null, Utfall.OPPFYLT, "") }
            .konklusjonFor(vurderingsperiode)
            .shouldBe(Konklusjon.Oppfylt(vurderingsperiode to vilkår.toSet()))
    }

    @Test
    fun `Skal få ikke-oppfylt når minst en vurdering ikke er oppfylt for hele perioden`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
        val vilkår: List<Vilkår> = listOf(
            Vilkår.DAGPENGER,
            Vilkår.AAP,
            Vilkår.ALDERSPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.OMSORGSPENGER,
            Vilkår.SYKEPENGER,
            Vilkår.KVP,
            Vilkår.INTROPROGRAMMET
        )

        vilkår
            .map { Vurdering(it, "", null, null, Utfall.OPPFYLT, "") }
            .plus(
                Vurdering(
                    Vilkår.GJENLEVENDEPENSJON,
                    "",
                    vurderingsperiode.fra,
                    vurderingsperiode.til,
                    Utfall.IKKE_OPPFYLT,
                    ""
                )
            )
            .konklusjonFor(vurderingsperiode)
            .shouldBe(Konklusjon.IkkeOppfylt(vurderingsperiode to setOf(Vilkår.GJENLEVENDEPENSJON)))
    }

    @Test
    fun `Skal få delvis oppfylt når en vurdering ikke er oppfylt for deler av perioden`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
        val vilkår: List<Vilkår> = listOf(
            Vilkår.DAGPENGER,
            Vilkår.AAP,
            Vilkår.ALDERSPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.OMSORGSPENGER,
            Vilkår.SYKEPENGER,
            Vilkår.KVP,
            Vilkår.INTROPROGRAMMET
        )

        vilkår
            .map { Vurdering(it, "", null, null, Utfall.OPPFYLT, "") }
            .plus(
                Vurdering(
                    Vilkår.GJENLEVENDEPENSJON,
                    "",
                    LocalDate.of(2022, 1, 11),
                    vurderingsperiode.til,
                    Utfall.IKKE_OPPFYLT,
                    ""
                )
            )
            .konklusjonFor(vurderingsperiode)
            .shouldBe(
                Konklusjon.DelvisOppfylt(
                    listOf(
                        Konklusjon.Oppfylt(
                            Periode(
                                vurderingsperiode.fra,
                                LocalDate.of(2022, 1, 10)
                            ) to vilkår.toSet()
                        )
                    ),
                    listOf(
                        Konklusjon.IkkeOppfylt(
                            Periode(
                                LocalDate.of(2022, 1, 11),
                                vurderingsperiode.til
                            ) to setOf(Vilkår.GJENLEVENDEPENSJON)
                        )
                    )
                )
            )
    }
}
