package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.kvpJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeNei
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import org.junit.jupiter.api.Test

class KommunaleYtelserVilkårsvurderingTest {

    @Test
    fun `Samlet utfall for kommunale ytelser`() {
        val søknad = nySøknadMedTiltak(
            intro = periodeNei(),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kommunaleYtelserVilkårsvurderingKategori =
            KommunaleYtelserVilkårsvurderingKategori(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.OPPFYLT
        kommunaleYtelserVilkårsvurderingKategori.vilkår().lovreferanse.paragraf shouldBe "§7"
    }

    @Test
    fun `Samlet utfall for kommunale ytelser som krever manuell behandling`() {
        val søknad = nySøknadMedTiltak(
            kvp = kvpJa(),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kommunaleYtelserVilkårsvurderingKategori =
            KommunaleYtelserVilkårsvurderingKategori(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
