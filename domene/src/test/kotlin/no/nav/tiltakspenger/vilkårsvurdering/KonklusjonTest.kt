package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class KonklusjonTest {

    private fun Vilkår.toOppfyltVurdering() = Vurdering(this, "", null, null, Utfall.OPPFYLT, "")

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
            Vilkår.INTROPROGRAMMET,
        )

        vilkår
            .map { it.toOppfyltVurdering() }
            .konklusjonFor(vurderingsperiode)
            .shouldBe(Konklusjon.Oppfylt(vurderingsperiode to vilkår.map { it.toOppfyltVurdering() }.toSet()))
    }

    @Test
    fun `Teste range av ikke godkjente`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 30))
        val vilkår: List<Vilkår> = listOf(Vilkår.DAGPENGER, Vilkår.AAP)
        val gjenlevendepensjonVurdering = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 1, 7),
            Utfall.IKKE_OPPFYLT,
            "",
        )
        val lønnsinntektVurdering = Vurdering(
            Vilkår.LØNNSINNTEKT,
            "",
            LocalDate.of(2022, 1, 5),
            LocalDate.of(2022, 1, 10),
            Utfall.IKKE_OPPFYLT,
            "",
        )
        val sykepengerVurdering = Vurdering(
            Vilkår.SYKEPENGER,
            "",
            LocalDate.of(2022, 1, 9),
            LocalDate.of(2022, 1, 15),
            Utfall.IKKE_OPPFYLT,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendepensjonVurdering)
            .plus(lønnsinntektVurdering)
            .plus(sykepengerVurdering)
            .konklusjonFor(vurderingsperiode)
            .shouldBe(
                Konklusjon.DelvisOppfylt(
                    listOf(
                        Konklusjon.Oppfylt(
                            Periode(
                                LocalDate.of(2022, 1, 16),
                                LocalDate.of(2022, 1, 30),
                            ) to vilkår.map { it.toOppfyltVurdering() }.toSet(),
                        ),
                    ),
                    listOf(
                        Konklusjon.IkkeOppfylt(
                            Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 15)) to setOf(
                                gjenlevendepensjonVurdering,
                                lønnsinntektVurdering,
                                sykepengerVurdering,
                            ),
                        ),
                    ),
                ),
            )
    }

    @Test
    fun `Teste manuelle periode som ikke overlapper`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 30))
        val vilkår: List<Vilkår> = listOf(Vilkår.DAGPENGER, Vilkår.AAP)

        val gjenlevendeVurdering = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 1, 7),
            Utfall.KREVER_MANUELL_VURDERING,
            "",
        )
        val lønnsinntektVurdering = Vurdering(
            Vilkår.LØNNSINNTEKT,
            "",
            LocalDate.of(2022, 1, 5),
            LocalDate.of(2022, 1, 9),
            Utfall.KREVER_MANUELL_VURDERING,
            "",
        )
        val sykepengerVurdering = Vurdering(
            Vilkår.SYKEPENGER,
            "",
            LocalDate.of(2022, 1, 12),
            LocalDate.of(2022, 1, 15),
            Utfall.KREVER_MANUELL_VURDERING,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering)
            .plus(lønnsinntektVurdering)
            .plus(sykepengerVurdering)
            .konklusjonFor(vurderingsperiode)
            .shouldBe(
                Konklusjon.KreverManuellBehandling(
                    mapOf(

                        Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 9)) to setOf(
                            gjenlevendeVurdering,
                            lønnsinntektVurdering,
                        ),
                        Periode(LocalDate.of(2022, 1, 12), LocalDate.of(2022, 1, 15)) to setOf(
                            sykepengerVurdering,
                        ),

                    ),
                ),
            )
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
            Vilkår.INTROPROGRAMMET,
        )

        val gjenlevendeVurdering = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            vurderingsperiode.fra,
            vurderingsperiode.til,
            Utfall.IKKE_OPPFYLT,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering)
            .konklusjonFor(vurderingsperiode)
            .shouldBe(Konklusjon.IkkeOppfylt(vurderingsperiode to setOf(gjenlevendeVurdering)))
    }

    @Test
    fun `Skal få ikke-oppfylt når to vurderinger tilsammen ikke er oppfylt for hele perioden`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
        val vilkår: List<Vilkår> = listOf(
            Vilkår.DAGPENGER,
            Vilkår.AAP,
            Vilkår.ALDERSPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.OMSORGSPENGER,
            Vilkår.SYKEPENGER,
            Vilkår.KVP,
            Vilkår.INTROPROGRAMMET,
        )

        val gjenlevendeVurdering1 = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            vurderingsperiode.fra,
            LocalDate.of(2022, 1, 10),
            Utfall.IKKE_OPPFYLT,
            "",
        )
        val gjenlevendeVurdering2 = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            LocalDate.of(2022, 1, 11),
            vurderingsperiode.til,
            Utfall.IKKE_OPPFYLT,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering1)
            .plus(gjenlevendeVurdering2)
            .konklusjonFor(vurderingsperiode)
            .shouldBe(Konklusjon.IkkeOppfylt(vurderingsperiode to setOf(gjenlevendeVurdering1, gjenlevendeVurdering2)))
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
            Vilkår.INTROPROGRAMMET,
        )

        val gjenlevendeVurdering = Vurdering(
            Vilkår.GJENLEVENDEPENSJON,
            "",
            LocalDate.of(2022, 1, 11),
            vurderingsperiode.til,
            Utfall.IKKE_OPPFYLT,
            "",
        )
        vilkår
            .map { Vurdering(it, "", null, null, Utfall.OPPFYLT, "") }
            .plus(gjenlevendeVurdering)
            .konklusjonFor(vurderingsperiode)
            .shouldBe(
                Konklusjon.DelvisOppfylt(
                    listOf(
                        Konklusjon.Oppfylt(
                            Periode(
                                vurderingsperiode.fra,
                                LocalDate.of(2022, 1, 10),
                            ) to vilkår.map { it.toOppfyltVurdering() }.toSet(),
                        ),
                    ),
                    listOf(
                        Konklusjon.IkkeOppfylt(
                            Periode(
                                LocalDate.of(2022, 1, 11),
                                vurderingsperiode.til,
                            ) to setOf(gjenlevendeVurdering),
                        ),
                    ),
                ),
            )
    }
}
