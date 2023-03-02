package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import org.junit.jupiter.api.Test

class ForeldrepengerVilkårsvurderingTest {

    @Test
    fun `Vilkåret om foreldrepenger blir oppfylt når bruker ikke har noe vedtak`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val foreldrepengerVilkårsvurdering = ForeldrepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = emptyList(),
        )

        foreldrepengerVilkårsvurdering.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Vilkåret om foreldrepenger blir manuell når bruker har vedtak i perioden`() {
        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))
        val foreldrepengerVilkårsvurdering = ForeldrepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            ytelser = listOf(
                ObjectMother.foreldrepengerVedtak(
                    periode = vurderingsperiode,
                ),
            ),
        )

        foreldrepengerVilkårsvurdering.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
