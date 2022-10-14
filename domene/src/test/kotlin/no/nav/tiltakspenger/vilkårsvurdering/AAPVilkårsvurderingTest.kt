package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.domene.mars
import no.nav.tiltakspenger.domene.marsDateTime
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.junit.jupiter.api.Test

internal class AAPVilkårsvurderingTest {

    @Test
    fun `skal bli oppfylt`() {
        val ytelser = listOf(
            ytelseSak(
                fomGyldighetsperiode = 1.januarDateTime(2022),
                tomGyldighetsperiode = 31.januarDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
            ytelseSak(
                fomGyldighetsperiode = 1.marsDateTime(2022),
                tomGyldighetsperiode = 31.marsDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
        )
        val vurderingsperiode = Periode(19.februar(2022), 28.februar(2022))

        val aapVilkårsvurdering =
            AAPVilkårsvurdering(ytelser = ytelser, vurderingsperiode = vurderingsperiode)

        aapVilkårsvurdering.vurderinger().first().kilde shouldBe "Arena"
        aapVilkårsvurdering.vurderinger().first().fom shouldBe null
        aapVilkårsvurdering.vurderinger().first().tom shouldBe null
        aapVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT
        aapVilkårsvurdering.lovReferanse.paragraf shouldBe "§7"
        aapVilkårsvurdering.lovReferanse.ledd shouldBe "1"
    }

    @Test
    fun `skal ikke bli oppfylt i begge ender`() {
        val ytelser = listOf(
            ytelseSak(
                fomGyldighetsperiode = 1.januarDateTime(2022),
                tomGyldighetsperiode = 31.januarDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
            ytelseSak(
                fomGyldighetsperiode = 1.marsDateTime(2022),
                tomGyldighetsperiode = 31.marsDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
        )
        val vurderingsperiode = Periode(19.januar(2022), 28.mars(2022))

        val aapVilkårsvurdering =
            AAPVilkårsvurdering(ytelser = ytelser, vurderingsperiode = vurderingsperiode)

        aapVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
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
        aapVilkårsvurdering.lovReferanse.paragraf shouldBe "§7"
        aapVilkårsvurdering.lovReferanse.ledd shouldBe "1"
    }

    @Test
    fun `skal bli oppfylt fordi den overstyres manuelt`() {
        val ytelser = listOf(
            ytelseSak(
                fomGyldighetsperiode = 1.januarDateTime(2022),
                tomGyldighetsperiode = 31.januarDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
            ytelseSak(
                fomGyldighetsperiode = 1.marsDateTime(2022),
                tomGyldighetsperiode = 31.marsDateTime(2022),
                ytelsestype = YtelseSak.YtelseSakYtelsetype.AA
            ),
        )
        val vurderingsperiode = Periode(19.januar(2022), 28.mars(2022))

        val aapVilkårsvurdering =
            AAPVilkårsvurdering(ytelser = ytelser, vurderingsperiode = vurderingsperiode)

        aapVilkårsvurdering.settManuellVurdering(
            fom = 19.januar(2022),
            tom = 28.mars(2022),
            utfall = Utfall.OPPFYLT,
            detaljer = "",
        )

        aapVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            Vurdering(
                kilde = "Arena",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
            Vurdering(
                kilde = "Saksbehandler",
                fom = 1.mars(2022),
                tom = 31.mars(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = ""
            ),
            Vurdering(
                kilde = "Arena",
                fom = 19.januar(2022),
                tom = 28.mars(2022),
                utfall = Utfall.OPPFYLT,
                detaljer = ""
            )
        )
        aapVilkårsvurdering.samletVurdering shouldBe Utfall.OPPFYLT
        aapVilkårsvurdering.lovReferanse.paragraf shouldBe "§7"
        aapVilkårsvurdering.lovReferanse.ledd shouldBe "1"
    }
}
