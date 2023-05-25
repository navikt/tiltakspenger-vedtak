package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.felles.marsDateTime
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype.AA
import no.nav.tiltakspenger.vedtak.YtelseSak.YtelseSakYtelsetype.DAGP
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@Disabled
internal class StatligArenaYtelseVilkårsvurderingTest {

    companion object {

        @JvmStatic
        private fun utenOverlapp() = testdata(vurderingsperiode = Periode(19.februar(2022), 28.februar(2022)))

        @JvmStatic
        private fun medOverlapp() = testdata(vurderingsperiode = Periode(19.januar(2022), 28.mars(2022)))

        private fun testdata(vurderingsperiode: Periode) = listOf(
            Arguments.of(AAPVilkårsvurdering(ytelser(AA), vurderingsperiode)),
            Arguments.of(DagpengerVilkårsvurdering(ytelser(DAGP), vurderingsperiode)),
        )

        private fun ytelser(type: YtelseSak.YtelseSakYtelsetype) = listOf(
            ytelseSak(
                fomGyldighetsperiode = 1.januarDateTime(2022),
                tomGyldighetsperiode = 31.januarDateTime(2022),
                ytelsestype = type,
            ),
            ytelseSak(
                fomGyldighetsperiode = 1.marsDateTime(2022),
                tomGyldighetsperiode = 31.marsDateTime(2022),
                ytelsestype = type,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("utenOverlapp")
    fun `vilkåret er oppfylt når vurderingsperioden ikke overlapper med perioden for ytelsen`(
        statligVilkårsvurdering: StatligArenaYtelseVilkårsvurdering,
    ) {
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.vurderinger().first().kilde shouldBe "Arena"
        statligVilkårsvurdering.vurderinger().first().fom shouldBe null
        statligVilkårsvurdering.vurderinger().first().tom shouldBe null
        statligVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "1"
    }

    @ParameterizedTest
    @MethodSource("medOverlapp")
    fun `vilkåret er ikke oppfylt når vurderingsperioden overlapper med perioden for ytelsen`(
        statligVilkårsvurdering: StatligArenaYtelseVilkårsvurdering,
    ) {
        statligVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.SYKEPENGER,
                kilde = "Arena",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                detaljer = "",
            ),
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.SYKEPENGER,
                kilde = "Arena",
                fom = 1.mars(2022),
                tom = 31.mars(2022),
                detaljer = "",
            ),
        )
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        statligVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "1"
    }

    @ParameterizedTest
    @MethodSource("medOverlapp")
    fun `vilkåret er oppfylt fordi den overstyres manuelt`(statligVilkårsvurdering: StatligArenaYtelseVilkårsvurdering) {
        statligVilkårsvurdering.settManuellVurdering(
            fom = 19.januar(2022),
            tom = 28.mars(2022),
            utfall = Utfall.OPPFYLT,
            detaljer = "",
        )

        statligVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.SYKEPENGER,
                kilde = "Arena",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                detaljer = "",
            ),
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.SYKEPENGER,
                kilde = "Arena",
                fom = 1.mars(2022),
                tom = 31.mars(2022),
                detaljer = "",
            ),
            Vurdering.Oppfylt(
                vilkår = Vilkår.SYKEPENGER,
                kilde = "Saksbehandler",
                detaljer = "",
            ),
        )
        statligVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
        statligVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        statligVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "1"
    }
}
