package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AnbefalingTest {

    private fun Vilkår.toOppfyltVurdering() = Vurdering.Oppfylt(
        vilkår = this,
        kilde = Kilde.SAKSB,
        fom = LocalDate.of(2022, 1, 1),
        tom = LocalDate.of(2022, 1, 31),
        detaljer = "",
    )

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
            .anbefalingFor(vurderingsperiode)
            .shouldBe(Anbefaling.Oppfylt(vurderingsperiode to vilkår.map { it.toOppfyltVurdering() }.toSet()))
    }

    @Test
    fun `Teste range av ikke godkjente`() {
        val vurderingsperiode = Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 30))
        val vilkår: List<Vilkår> = listOf(Vilkår.DAGPENGER, Vilkår.AAP)
        val gjenlevendepensjonVurdering = Vurdering.IkkeOppfylt(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 1, 7),
            "",
        )
        val lønnsinntektVurdering = Vurdering.IkkeOppfylt(
            Vilkår.LØNNSINNTEKT,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 5),
            LocalDate.of(2022, 1, 10),
            "",
        )
        val sykepengerVurdering = Vurdering.IkkeOppfylt(
            Vilkår.SYKEPENGER,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 9),
            LocalDate.of(2022, 1, 15),
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendepensjonVurdering)
            .plus(lønnsinntektVurdering)
            .plus(sykepengerVurdering)
            .anbefalingFor(vurderingsperiode)
            .shouldBe(
                Anbefaling.DelvisOppfylt(
                    listOf(
                        Anbefaling.Oppfylt(
                            Periode(
                                LocalDate.of(2022, 1, 16),
                                LocalDate.of(2022, 1, 30),
                            ) to vilkår.map { it.toOppfyltVurdering() }.toSet(),
                        ),
                    ),
                    listOf(
                        Anbefaling.IkkeOppfylt(
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

        val gjenlevendeVurdering = Vurdering.KreverManuellVurdering(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 1, 7),
            "",
        )
        val lønnsinntektVurdering = Vurdering.KreverManuellVurdering(
            Vilkår.LØNNSINNTEKT,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 5),
            LocalDate.of(2022, 1, 9),
            "",
        )
        val sykepengerVurdering = Vurdering.KreverManuellVurdering(
            Vilkår.SYKEPENGER,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 12),
            LocalDate.of(2022, 1, 15),
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering)
            .plus(lønnsinntektVurdering)
            .plus(sykepengerVurdering)
            .anbefalingFor(vurderingsperiode)
            .shouldBe(
                Anbefaling.KreverManuellBehandling(
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

        val gjenlevendeVurdering = Vurdering.IkkeOppfylt(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            vurderingsperiode.fra,
            vurderingsperiode.til,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering)
            .anbefalingFor(vurderingsperiode)
            .shouldBe(Anbefaling.IkkeOppfylt(vurderingsperiode to setOf(gjenlevendeVurdering)))
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

        val gjenlevendeVurdering1 = Vurdering.IkkeOppfylt(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            vurderingsperiode.fra,
            LocalDate.of(2022, 1, 10),
            "",
        )
        val gjenlevendeVurdering2 = Vurdering.IkkeOppfylt(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 11),
            vurderingsperiode.til,
            "",
        )
        vilkår
            .map { it.toOppfyltVurdering() }
            .plus(gjenlevendeVurdering1)
            .plus(gjenlevendeVurdering2)
            .anbefalingFor(vurderingsperiode)
            .shouldBe(Anbefaling.IkkeOppfylt(vurderingsperiode to setOf(gjenlevendeVurdering1, gjenlevendeVurdering2)))
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

        val gjenlevendeVurdering = Vurdering.IkkeOppfylt(
            Vilkår.GJENLEVENDEPENSJON,
            Kilde.SAKSB,
            LocalDate.of(2022, 1, 11),
            vurderingsperiode.til,
            "",
        )
        vilkår
            .map { Vurdering.Oppfylt(it, Kilde.SAKSB, "", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31)) }
            .plus(gjenlevendeVurdering)
            .anbefalingFor(vurderingsperiode)
            .shouldBe(
                Anbefaling.DelvisOppfylt(
                    listOf(
                        Anbefaling.Oppfylt(
                            Periode(
                                vurderingsperiode.fra,
                                LocalDate.of(2022, 1, 10),
                            ) to vilkår.map { it.toOppfyltVurdering() }.toSet(),
                        ),
                    ),
                    listOf(
                        Anbefaling.IkkeOppfylt(
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
