package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import org.junit.jupiter.api.Test

class KommunaleYtelserVilkårsvurderingTest {

    @Test
    fun `Samlet utfall for kommunale ytelser`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kommunaleYtelserVilkårsvurderinger =
            KommunaleYtelserVilkårsvurderinger(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderinger.samletUtfall() shouldBe Utfall.OPPFYLT
        kommunaleYtelserVilkårsvurderinger.lovreferanse().paragraf shouldBe "§7"
    }

    @Test
    fun `Samlet utfall for kommunale ytelser som krever manuell behandling`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kommunaleYtelserVilkårsvurderinger =
            KommunaleYtelserVilkårsvurderinger(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderinger.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
