package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import org.junit.jupiter.api.Test

class UklartPeriodeSpmVurderingTest {

    @Test
    fun testJaSpmGammelSøknad() {
        val vilkårsvurdering = UklartPeriodeSpmVurdering(
            spm = Søknad.PeriodeSpm.Ja(
                periode = Periode(1.januar(2008), 10.januar(2008)),
            ),
            søknadVersjon = "1",
            vilkår = Vilkår.INTROPROGRAMMET,
            vurderingsperiode = Periode(1.januar(2007), 10.januar(2009)),
        )
        val vurdering = vilkårsvurdering.lagVurderingFraSøknad()
        vurdering.fom shouldBe 1.januar(2008)
        vurdering.utfall shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun testJaSpmNySøknad() {
        val vilkårsvurdering = UklartPeriodeSpmVurdering(
            spm = Søknad.PeriodeSpm.Ja(
                periode = Periode(1.januar(2008), 10.januar(2008)),
            ),
            søknadVersjon = "2",
            vilkår = Vilkår.INTROPROGRAMMET,
            vurderingsperiode = Periode(1.januar(2007), 10.januar(2009)),
        )
        val vurdering = vilkårsvurdering.lagVurderingFraSøknad()
        vurdering.fom shouldBe 1.januar(2008)
        vurdering.utfall shouldBe Utfall.IKKE_OPPFYLT
    }
}
