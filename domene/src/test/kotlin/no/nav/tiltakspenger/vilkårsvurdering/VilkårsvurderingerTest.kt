package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.februarDateTime
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import org.junit.jupiter.api.Test

class VilkårsvurderingerTest {

    @Test
    fun `en vilkårsvurdering med kvp skal gi manuell vurdering`() {

        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))
        val søknad = nySøknadMedArenaTiltak(
            deltarKvp = true,
        )

        val vilkårsvurderinger = Vilkårsvurderinger(
            statligeYtelserVilkårsvurderinger = StatligeYtelserVilkårsvurderinger(
                aap = AAPVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
                dagpenger = DagpengerVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
            ),
            kommunaleYtelserVilkårsvurderinger = KommunaleYtelserVilkårsvurderinger(
                intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
                kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        vilkårsvurderinger.ikkeOppfylteVurderinger.first().periode shouldBe vurderingsperiode

    }

    @Test
    fun `en vilkårsvurdering med intro skal gi manuell vurdering`() {

        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = true,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
            ),
        )

        val vilkårsvurderinger = Vilkårsvurderinger(
            statligeYtelserVilkårsvurderinger = StatligeYtelserVilkårsvurderinger(
                aap = AAPVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
                dagpenger = DagpengerVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
            ),
            kommunaleYtelserVilkårsvurderinger = KommunaleYtelserVilkårsvurderinger(
                intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
                kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        vilkårsvurderinger.ikkeOppfylteVurderinger.first().periode shouldBe vurderingsperiode

    }

    @Test
    fun `en vilkårsvurdering med dagpenger skal gi ikke oppfylt og riktig periode`() {

        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                fom = vurderingsperiode.fra,
                tom = vurderingsperiode.til,
            ),
        )

        val vilkårsvurderinger = Vilkårsvurderinger(
            statligeYtelserVilkårsvurderinger = StatligeYtelserVilkårsvurderinger(
                aap = AAPVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
                dagpenger = DagpengerVilkårsvurdering(
                    ytelser = listOf(
                        ytelseSak(
                            fomGyldighetsperiode = 3.februarDateTime(2022),
                            tomGyldighetsperiode = 15.februarDateTime(2022),
                        )
                    ), vurderingsperiode = vurderingsperiode
                ),
            ),
            kommunaleYtelserVilkårsvurderinger = KommunaleYtelserVilkårsvurderinger(
                intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
                kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        vilkårsvurderinger.ikkeOppfylteVurderinger.first().periode shouldBe Periode(3.februar(2022), 15.februar(2022))
        vilkårsvurderinger.ikkeOppfylteVurderinger.first().lovreferanse shouldBe Lovreferanse.DAGPENGER

    }

    @Test
    fun `en oppfylt vilkårsvurdering`() {

        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))
        val søknad = nySøknadMedArenaTiltak(
            deltarIntroduksjonsprogrammet = false,
        )

        val vilkårsvurderinger = Vilkårsvurderinger(
            statligeYtelserVilkårsvurderinger = StatligeYtelserVilkårsvurderinger(
                aap = AAPVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
                dagpenger = DagpengerVilkårsvurdering(ytelser = emptyList(), vurderingsperiode = vurderingsperiode),
            ),
            kommunaleYtelserVilkårsvurderinger = KommunaleYtelserVilkårsvurderinger(
                intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
                kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.OPPFYLT

    }
}
