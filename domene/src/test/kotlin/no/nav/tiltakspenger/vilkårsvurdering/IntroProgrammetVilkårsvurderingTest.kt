package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.objectmothers.arenaTiltak
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import no.nav.tiltakspenger.vedtak.Søknad
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class IntroProgrammetVilkårsvurderingTest {

    @Test
    fun `Kunne sende inn en søknad i vilkårsvurdering`() {
        val søknad = Søknad(
            id = SøknadId.random(),
            søknadId = "1234",
            journalpostId = "123",
            dokumentInfoId = "123",
            fornavn = null,
            etternavn = null,
            ident = "",
            deltarKvp = false,
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = 1 januar (2022),
                tom = 31 januar (2022),
            ),
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = listOf(),
            tidsstempelHosOss = LocalDateTime.now(),
            tiltak = arenaTiltak(),
            trygdOgPensjon = listOf(),
            fritekst = null
        )

        val introProgrammetVilkårsvurdering = IntroProgrammetVilkårsvurdering(søknad = søknad)

        introProgrammetVilkårsvurdering.vurderinger.first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger.first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger.first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger.first().utfall shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `Kunne vurdere en søknad hvor vilkåret er oppfylt`() {
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

        val introProgrammetVilkårsvurdering = IntroProgrammetVilkårsvurdering(søknad = søknad)

        introProgrammetVilkårsvurdering.vurderinger.first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger.first().fom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger.first().tom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger.first().utfall shouldBe Utfall.OPPFYLT
    }

}
