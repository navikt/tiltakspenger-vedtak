package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.oktober
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import org.junit.jupiter.api.Test

internal class KVPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(
                deltarKvp = søknad.deltarKvp,
                vurderingsperiode = vurderingsperiode,
            )

        kvpVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        kvpVilkårsvurdering.vurderinger().first().fom shouldBe null
        kvpVilkårsvurdering.vurderinger().first().tom shouldBe null
        kvpVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        kvpVilkårsvurdering.vurderinger().first().detaljer shouldBe ""
        kvpVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        kvpVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `En vilkårsvurdering har en søknad og manuell vurdering`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(
                deltarKvp = søknad.deltarKvp,
                vurderingsperiode = vurderingsperiode,
            )

        kvpVilkårsvurdering.settManuellVurdering(
            fom = 1.januar(2022),
            tom = 31.oktober(2022),
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = "",
        )
        val vurderingSøknad =
            Vurdering(
                vilkår = Vilkår.KVP,
                kilde = "Søknad",
                fom = null,
                tom = null,
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = "",
            )
        val vurderingSaksbehandler = Vurdering(
            vilkår = Vilkår.KVP,
            kilde = "Saksbehandler",
            fom = 1.januar(2022),
            tom = 31.oktober(2022),
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = "",
        )
        kvpVilkårsvurdering.vurderinger().size shouldBe 2
        kvpVilkårsvurdering.vurderinger().first { it.kilde == "Søknad" }
            .shouldBeEqualToIgnoringFields(vurderingSøknad, Vurdering::tidspunkt)
        kvpVilkårsvurdering.vurderinger().first { it.kilde == "Saksbehandler" }
            .shouldBeEqualToIgnoringFields(vurderingSaksbehandler, Vurdering::tidspunkt)

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

}
