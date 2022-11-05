package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
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
            IntroProgrammetVilkårsvurdering(
                deltarIntroduksjonsprogrammet = søknad.deltarIntroduksjonsprogrammet,
                introduksjonsprogrammetDetaljer = søknad.introduksjonsprogrammetDetaljer,
                vurderingsperiode = vurderingsperiode,
            )

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(
                deltarKvp = søknad.deltarKvp,
                vurderingsperiode = vurderingsperiode,
            )

        val kommunaleYtelserVilkårsvurderingKategori =
            KommunaleYtelserVilkårsvurderingKategori(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.OPPFYLT
        kommunaleYtelserVilkårsvurderingKategori.vilkår().lovreferanse.paragraf shouldBe "§7"
    }

    @Test
    fun `Samlet utfall for kommunale ytelser som krever manuell behandling`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(
                deltarIntroduksjonsprogrammet = søknad.deltarIntroduksjonsprogrammet,
                introduksjonsprogrammetDetaljer = søknad.introduksjonsprogrammetDetaljer,
                vurderingsperiode = vurderingsperiode,
            )

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(
                deltarKvp = søknad.deltarKvp,
                vurderingsperiode = vurderingsperiode,
            )

        val kommunaleYtelserVilkårsvurderingKategori =
            KommunaleYtelserVilkårsvurderingKategori(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
