package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.oktober
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import org.junit.jupiter.api.Test

internal class KVPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        kvpVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        kvpVilkårsvurdering.vurderinger().first().fom shouldBe null
        kvpVilkårsvurdering.vurderinger().first().tom shouldBe null
        kvpVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        kvpVilkårsvurdering.vurderinger().first().detaljer shouldBe ""
        kvpVilkårsvurdering.lovreferanse.paragraf shouldBe "§7"
        kvpVilkårsvurdering.lovreferanse.ledd shouldBe "3"

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `En vilkårsvurdering har en søknad og manuell vurdering`() {
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        kvpVilkårsvurdering.settManuellVurdering(
            fom = 1.januar(2022),
            tom = 31.oktober(2022),
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = "",
        )
        val vurderingSøknad =
            Vurdering(
                kilde = "Søknad",
                fom = null,
                tom = null,
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = "",
            )
        val vurderingSaksbehandler = Vurdering(
            kilde = "Saksbehandler",
            fom = 1.januar(2022),
            tom = 31.oktober(2022),
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = "",
        )
        kvpVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            vurderingSøknad,
            vurderingSaksbehandler
        )

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

}
