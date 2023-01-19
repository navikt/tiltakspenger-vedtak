package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderVilkårsvurdering
import org.junit.jupiter.api.Test

class AlderVilkårsvurderingTest {

    @Test
    fun `Vilkåret om alder blir oppfylt når bruker er 18 år i hele vurderingsperioden`() {
        val fødselsdato = 1.januar(2022).minusYears(19)
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val alderVilkårsvurdering = AlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søkersFødselsdato = fødselsdato
        )
        alderVilkårsvurdering.alderVurderinger.size shouldBe 1
        alderVilkårsvurdering.alderVurderinger[0].utfall shouldBe Utfall.OPPFYLT
        alderVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Vilkåret om alder blir delvis oppfylt når bruker fyller 18 år i løpet av vurderingsperioden`() {
        val fødselsdato = 15.januar(2022).minusYears(18)
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val alderVilkårsvurdering = AlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søkersFødselsdato = fødselsdato
        )
        alderVilkårsvurdering.alderVurderinger.size shouldBe 1
        alderVilkårsvurdering.alderVurderinger[0].utfall shouldBe Utfall.IKKE_OPPFYLT
        alderVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `Vilkåret om alder blir ikke oppfylt når bruker er under 18 år i hele vurderingsperioden`() {
        val fødselsdato = 1.januar(2022).minusYears(17)
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val alderVilkårsvurdering = AlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            søkersFødselsdato = fødselsdato
        )
        alderVilkårsvurdering.alderVurderinger.size shouldBe 1
        alderVilkårsvurdering.alderVurderinger[0].utfall shouldBe Utfall.IKKE_OPPFYLT
        alderVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
    }
}
