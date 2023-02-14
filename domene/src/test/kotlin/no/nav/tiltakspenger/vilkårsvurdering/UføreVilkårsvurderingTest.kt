package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.desember
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføreVilkarsvurdering
import org.junit.jupiter.api.Test

class UføreVilkårsvurderingTest {

    @Test
    fun `Vilkåret om uføre blir oppfylt når bruker ikke har noe vedtak`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = null,
        )
        uføreVilkarsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Vilkåret om uføre blir ikke oppfylt når bruker har vedtak i hele perioden`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = ObjectMother.uføreVedtak(
                harUføregrad = true,
                datoUfør = 31.desember(2021),
                virkDato = 31.desember(2021),
            ),
        )
        uføreVilkarsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Vilkåret om uføre blir ikke oppfylt når bruker har vedtak som starter likt med perioden`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = ObjectMother.uføreVedtak(
                harUføregrad = true,
                datoUfør = 1.januar(2022),
                virkDato = 1.januar(2022),
            ),
        )
        uføreVilkarsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Vilkåret om uføre blir delvis oppfylt når bruker har vedtak i løpet av perioden`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = ObjectMother.uføreVedtak(
                harUføregrad = true,
                datoUfør = 15.januar(2022),
                virkDato = 15.januar(2022),
            ),
        )
        uføreVilkarsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        uføreVilkarsvurdering.uføreVurderinger.size shouldBe 1
        uføreVilkarsvurdering.uføreVurderinger.first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Vilkåret om uføre blir ikke oppfylt når bruker har vedtak som starter før perioden`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = ObjectMother.uføreVedtak(
                harUføregrad = true,
                datoUfør = 1.januar(2021),
                virkDato = 1.januar(2021),
            ),
        )
        uføreVilkarsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
