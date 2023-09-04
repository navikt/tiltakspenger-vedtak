package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeNei
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import org.junit.jupiter.api.Test

internal class IntroProgrammetVilkårsvurderingTest {

    @Test
    fun `Kunne sende inn en søknad i vilkårsvurdering`() {
        val søknad = nySøknadMedTiltak(
            intro = periodeJa(
                fom = 1 januar (2022),
                tom = 31 januar (2022),
            ),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe Kilde.SØKNAD
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Kunne vurdere en søknad hvor vilkåret er oppfylt`() {
        val søknad = nySøknadMedTiltak(
            intro = periodeNei(),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe Kilde.SØKNAD
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe null
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.OPPFYLT

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Kunne sende inn en manuell vurdering`() {
        val søknad = nySøknadMedTiltak(
            intro = periodeNei(),
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

        val vurderingSøknad = Vurdering.Oppfylt(
            vilkår = Vilkår.INTROPROGRAMMET,
            kilde = Kilde.SØKNAD,
            detaljer = "Svart NEI i søknaden",
        )
        val vurderingSaksbehandler =
            Vurdering.IkkeOppfylt(
                vilkår = Vilkår.INTROPROGRAMMET,
                kilde = Kilde.SAKSB,
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                detaljer = "",
            )
        introProgrammetVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            vurderingSøknad,
            vurderingSaksbehandler,
        )

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `Kunne vurdere en søknad opp mot en vurderingsperiode i vilkårsvurdering`() {
        val søknad = nySøknadMedTiltak(
            intro = periodeJa(
                fom = 1 januar (2022),
                tom = 31 januar (2022),
            ),
        )

        val vurderingsperiode = Periode(1.februar(2022), 10.februar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe Kilde.SØKNAD
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().detaljer shouldBe "Svart JA i søknaden"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Kunne vurdere en søknad med mangelfulle data`() {
        val søknad = nySøknadMedTiltak(
            intro = Søknad.PeriodeSpm.FeilaktigBesvart(
                svartJa = true,
                fom = null,
                tom = null,
            ),
        )

        val vurderingsperiode = Periode(1.februar(2022), 10.februar(2022))

        val introProgrammetVilkårsvurdering =
            IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        introProgrammetVilkårsvurdering.vurderinger().first().kilde shouldBe Kilde.SØKNAD
        introProgrammetVilkårsvurdering.vurderinger().first().fom shouldBe 1.februar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().tom shouldBe 10.februar(2022)
        introProgrammetVilkårsvurdering.vurderinger().first().detaljer shouldBe "Feilaktig besvart i søknaden"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        introProgrammetVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"
        introProgrammetVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        introProgrammetVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
