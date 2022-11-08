package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import org.junit.jupiter.api.Test

internal class IntroProgrammetVilkårsvurderingTest {

    @Test
    fun `Kunne sende inn en søknad i vilkårsvurdering`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = 1 januar (2022),
                tom = 31 januar (2022),
            ),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Kunne vurdere en søknad hvor vilkåret er oppfylt`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
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
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.settManuellVurdering(
            fom = 1.januar(2022),
            tom = 31.januar(2022),
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = "",
        )

        val vurderingSøknad = Vurdering(
            vilkår = Vilkår.INTROPROGRAMMET,
            kilde = "Søknad",
            fom = null,
            tom = null,
            utfall = Utfall.OPPFYLT,
            detaljer = ""
        )
        val vurderingSaksbehandler =
            Vurdering(
                vilkår = Vilkår.INTROPROGRAMMET,
                kilde = "Saksbehandler",
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                utfall = Utfall.IKKE_OPPFYLT,
                detaljer = "",
            )
        introProgrammetVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            vurderingSøknad,
            vurderingSaksbehandler
        )

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `Kunne vurdere en søknad opp mot en vurderingsperiode i vilkårsvurdering`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = 1 januar (2022),
                tom = 31 januar (2022),
            ),
        )

        val vurderingsperiode = Periode(1.februar(2022), 10.februar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().detaljer shouldBe ""
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Kunne vurdere en søknad med mangelfulle data`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = null,
        )

        val vurderingsperiode = Periode(1.februar(2022), 10.februar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().detaljer shouldBe ""
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
