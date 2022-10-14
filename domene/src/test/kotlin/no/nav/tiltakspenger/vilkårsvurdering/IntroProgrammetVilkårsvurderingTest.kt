package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
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

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.IKKE_OPPFYLT

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
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

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Kunne sende inn en manuell vurdering`() {
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

        introProgrammetVilkårsvurdering.settManuellVurdering(
            fom = 1.januar(2022),
            tom = 31.januar(2022),
            utfall = Utfall.IKKE_OPPFYLT
        )

        val vurderingSøknad = Vurdering(kilde = "Søknad", fom = null, tom = null, utfall = Utfall.OPPFYLT)
        val vurderingSaksbehandler =
            Vurdering(
                kilde = "Saksbehandler",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                utfall = Utfall.IKKE_OPPFYLT
            )
        introProgrammetVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            vurderingSøknad,
            vurderingSaksbehandler
        )

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `Kunne vurdere en søknad opp mot en vurderingsperiode i vilkårsvurdering`() {
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

        val vurderingsperiode = Periode(1.februar(2022), 10.februar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().detaljer shouldBe ""
        introProgrammetVilkårsvurdering.lovReferanse.paragraf shouldBe "§7"
        introProgrammetVilkårsvurdering.lovReferanse.ledd shouldBe "3"
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }
}
