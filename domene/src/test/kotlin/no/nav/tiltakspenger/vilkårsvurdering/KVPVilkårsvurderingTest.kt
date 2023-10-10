package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.oktober
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import org.junit.jupiter.api.Test

internal class KVPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {
        val søknad = nySøknadMedTiltak(
            kvp = periodeJa(
                fom = 1.januar(2022),
                tom = 31.januar(2022),
            ),
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        kvpVilkårsvurdering.vurderinger().first().kilde shouldBe Kilde.SØKNAD
        kvpVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        kvpVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        kvpVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING

        kvpVilkårsvurdering.vurderinger().first().detaljer shouldBe "Svart JA i søknaden"
        kvpVilkårsvurdering.vilkår().lovreferanse.paragraf shouldBe "§7"
        kvpVilkårsvurdering.vilkår().lovreferanse.ledd shouldBe "3"

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `En vilkårsvurdering har en søknad og manuell vurdering`() {
        val søknad = nySøknadMedTiltak(
            kvp = periodeJa(
                fom = 1.januar(2022),
                tom = 31.januar(2022),
            ),
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
            Vurdering.KreverManuellVurdering(
                vilkår = Vilkår.KVP,
                kilde = Kilde.SØKNAD,
                fom = 1.januar(2022),
                tom = 31.januar(2022),
                detaljer = "Svart JA i søknaden",
            )
        val vurderingSaksbehandler = Vurdering.IkkeOppfylt(
            vilkår = Vilkår.KVP,
            kilde = Kilde.SAKSB,
            fom = 1.januar(2022),
            tom = 31.oktober(2022),
            detaljer = "",
        )
        kvpVilkårsvurdering.vurderinger() shouldContainExactlyInAnyOrder listOf(
            vurderingSøknad,
            vurderingSaksbehandler,
        )

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }
}
