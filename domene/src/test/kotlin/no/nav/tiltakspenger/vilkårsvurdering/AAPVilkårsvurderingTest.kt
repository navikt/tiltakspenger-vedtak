package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.ytelseSakAAP
import org.junit.jupiter.api.Test

internal class AAPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {
        val ytelser = ytelseSakAAP()
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val aapVilkårsvurdering =
            AAPVilkårsvurdering(ytelser = ytelser, vurderingsperiode = vurderingsperiode)

        aapVilkårsvurdering.vurderinger().first().kilde shouldBe "Arena"
        aapVilkårsvurdering.lovReferanse.paragraf shouldBe "§7"
        aapVilkårsvurdering.lovReferanse.ledd shouldBe "1"
    }
}
