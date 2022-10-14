package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.domene.marsDateTime
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.junit.jupiter.api.Test

internal class AAPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {
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
}
