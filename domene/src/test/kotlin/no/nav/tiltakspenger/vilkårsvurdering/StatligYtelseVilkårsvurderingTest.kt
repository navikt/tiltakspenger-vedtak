package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.februarDateTime
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.domene.mars
import no.nav.tiltakspenger.domene.marsDateTime
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype.AA
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype.DAGP
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StatligYtelseVilkårsvurderingTest {

    companion object {
        @Suppress("UnusedPrivateMember")
        @JvmStatic
        private fun utenOverlapp() = testdata(vurderingsperiode = Periode(19.februar(2022), 28.februar(2022)))

        @Suppress("UnusedPrivateMember")
        @JvmStatic
        private fun medOverlapp() = testdata(vurderingsperiode = Periode(19.januar(2022), 28.mars(2022)))

        private fun testdata(vurderingsperiode: Periode) = listOf(
            Arguments.of(AAPVilkårsvurdering(ytelser(AA), vurderingsperiode)),
            Arguments.of(DagpengerVilkårsvurdering(ytelser(DAGP), vurderingsperiode))
        )

        private fun ytelser(type: YtelseSak.YtelseSakYtelsetype) = listOf(
            ytelseSak(
                fomGyldighetsperiode = 1.januarDateTime(2022),
                tomGyldighetsperiode = 31.januarDateTime(2022),
                ytelsestype = type
            ),
            ytelseSak(
                fomGyldighetsperiode = 1.marsDateTime(2022),
                tomGyldighetsperiode = 31.marsDateTime(2022),
                ytelsestype = type
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("utenOverlapp")
    fun `vilkåret er oppfylt når vurderingsperioden ikke overlapper med perioden for ytelsen`(
        statligVilkårsvurdering: IStatligVilkårsvurdering
    ) {
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.vurderinger().first().kilde shouldBe "Arena"
        statligVilkårsvurdering.vurderinger().first().fom shouldBe null
        statligVilkårsvurdering.vurderinger().first().tom shouldBe null
        statligVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.lovreferanse.ledd shouldBe "1"
    }

    @ParameterizedTest
    @MethodSource("medOverlapp")
    fun `vilkåret er ikke oppfylt når vurderingsperioden overlapper med perioden for ytelsen`(
        statligVilkårsvurdering: IStatligVilkårsvurdering
    ) {
        statligVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            Vurdering(
                kilde = "Arena",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
            Vurdering(
                kilde = "Arena",
                fom = 1.mars(2022),
                tom = 31.mars(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
        )
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        statligVilkårsvurdering.lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.lovreferanse.ledd shouldBe "1"
    }

    @ParameterizedTest
    @MethodSource("medOverlapp")
    fun `vilkåret er oppfylt fordi den overstyres manuelt`(statligVilkårsvurdering: IStatligVilkårsvurdering) {
        statligVilkårsvurdering.settManuellVurdering(
            fom = 19.januar(2022),
            tom = 28.mars(2022),
            utfall = Utfall.OPPFYLT,
            detaljer = "",
        )

        statligVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            Vurdering(
                kilde = "Arena",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
            Vurdering(
                kilde = "Arena",
                fom = 1.mars(2022),
                tom = 31.mars(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
            Vurdering(
                kilde = "Saksbehandler",
                fom = 19.januar(2022),
                tom = 28.mars(2022),
                utfall = Utfall.OPPFYLT,
                detaljer = ""
            )
        )
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.lovreferanse.ledd shouldBe "1"
    }

    @Test
    fun `Samlet utfall for statlige ytelser, hvis 1 er ikke godkjent er ingen godkjent`() {
        val vurderingsperiode = Periode(1.februar(2022), 20.februar(2022))
        val aapVilkårsvurdering = AAPVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.januarDateTime(2022),
                    tomGyldighetsperiode = 31.januarDateTime(2022),
                    ytelsestype = AA,
                )
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val dagpengerVilkårsvurdering = DagpengerVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.februarDateTime(2022),
                    tomGyldighetsperiode = 28.februarDateTime(2022),
                    ytelsestype = DAGP,
                )
            ),
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingerKategori = StatligeYtelserVilkårsvurderingerKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
        )

        statligeYtelserVilkårsvurderingerKategori.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT

    }

    @Test
    fun `Samlet utfall for statlige ytelser, hvis begge er godkjent er alle godkjent`() {
        val vurderingsperiode = Periode(1.februar(2022), 20.februar(2022))
        val aapVilkårsvurdering = AAPVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val dagpengerVilkårsvurdering = DagpengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingerKategori = StatligeYtelserVilkårsvurderingerKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
        )

        statligeYtelserVilkårsvurderingerKategori.samletUtfall() shouldBe Utfall.OPPFYLT

    }
}
