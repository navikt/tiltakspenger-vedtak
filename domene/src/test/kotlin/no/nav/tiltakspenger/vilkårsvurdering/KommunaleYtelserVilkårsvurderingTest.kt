package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.objectmothers.arenaTiltak
import no.nav.tiltakspenger.vedtak.Søknad
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KommunaleYtelserVilkårsvurderingTest {

    @Test
    fun `Samlet utfall for kommunale ytelser`() {

        val søknad = Søknad(
            id = SøknadId.random(),
            søknadId = "1234",
            journalpostId = "123",
            dokumentInfoId = "123",
            fornavn = null,
            etternavn = null,
            ident = "",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = listOf(),
            tidsstempelHosOss = LocalDateTime.now(),
            tiltak = arenaTiltak(),
            trygdOgPensjon = listOf(),
            fritekst = null
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        val kommunaleYtelserVilkårsvurderinger =
            KommunaleYtelserVilkårsvurderinger(introProgrammetVilkårsvurdering, kvpVilkårsvurdering)
        kommunaleYtelserVilkårsvurderinger.samletUtfall() shouldBe Utfall.OPPFYLT
        kommunaleYtelserVilkårsvurderinger.lovReferanse.paragraf shouldBe "§7"
    }

    @Test
    fun `Samlet utfall for kommunale ytelser som krever manuell behandling`() {

        val søknad = Søknad(
            id = SøknadId.random(),
            søknadId = "1234",
            journalpostId = "123",
            dokumentInfoId = "123",
            fornavn = null,
            etternavn = null,
            ident = "",
            deltarKvp = true,
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = listOf(),
            tidsstempelHosOss = LocalDateTime.now(),
            tiltak = arenaTiltak(),
            trygdOgPensjon = listOf(),
            fritekst = null
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
