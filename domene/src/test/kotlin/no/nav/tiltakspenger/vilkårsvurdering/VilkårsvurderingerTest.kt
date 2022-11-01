package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.februarDateTime
import no.nav.tiltakspenger.objectmothers.nyDagpengerVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.nyIntroprogrammetVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.nyKommunaleYtelserVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.nyKvpVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.nyStatligeYtelserVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.nyVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ytelseSak
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.junit.jupiter.api.Test

class VilkårsvurderingerTest {

    @Test
    fun `en vilkårsvurdering med kvp skal gi manuell vurdering`() {
        val vilkårsvurderinger = nyVilkårsvurdering(
            kommunaleYtelserVilkårsvurderingKategori = nyKommunaleYtelserVilkårsvurdering(
                kvpVilkårsvurdering = nyKvpVilkårsvurdering(
                    søknad = nySøknadMedArenaTiltak(
                        deltarKvp = true,
                    )
                )
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        vilkårsvurderinger.vurderinger().ikkeOppfylte().size shouldBe 0
    }

    @Test
    fun `en vilkårsvurdering med intro skal gi manuell vurdering`() {
        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))
        val vilkårsvurderinger = nyVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            kommunaleYtelserVilkårsvurderingKategori = nyKommunaleYtelserVilkårsvurdering(
                vurderingsperiode = vurderingsperiode,
                introProgrammetVilkårsvurdering = nyIntroprogrammetVilkårsvurdering(
                    vurderingsperiode = vurderingsperiode,
                    søknad = nySøknadMedArenaTiltak(
                        deltarIntroduksjonsprogrammet = true,
                        introduksjonsprogrammetDetaljer = IntroduksjonsprogrammetDetaljer(
                            fom = vurderingsperiode.fra,
                            tom = vurderingsperiode.til,
                        ),
                    )
                )
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        vilkårsvurderinger.vurderinger().ikkeOppfylte().first().fom shouldBe vurderingsperiode.fra
        vilkårsvurderinger.vurderinger().ikkeOppfylte().first().tom shouldBe vurderingsperiode.til
    }

    @Test
    fun `en vilkårsvurdering med dagpenger skal gi ikke oppfylt og riktig periode`() {

        val vurderingsperiode = Periode(1.februar(2022), 28.februar(2022))

        val vilkårsvurderinger = nyVilkårsvurdering(
            statligeYtelserVilkårsvurderingKategori = nyStatligeYtelserVilkårsvurdering(
                dagpengerVilkårsvurdering = nyDagpengerVilkårsvurdering(
                    vurderingsperiode = vurderingsperiode,
                    ytelser = listOf(
                        ytelseSak(
                            ytelsestype = YtelseSak.YtelseSakYtelsetype.DAGP,
                            fomGyldighetsperiode = 3.februarDateTime(2022),
                            tomGyldighetsperiode = 15.februarDateTime(2022),
                        )
                    )
                )
            )
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT
        vilkårsvurderinger.vurderinger().ikkeOppfylte().first().fom shouldBe 3.februar(2022)
        vilkårsvurderinger.vurderinger().ikkeOppfylte().first().tom shouldBe 15.februar(2022)
        vilkårsvurderinger.vurderinger().ikkeOppfylte().first().vilkår shouldBe Vilkår.DAGPENGER

    }

    @Test
    fun `en oppfylt vilkårsvurdering`() {
        val vilkårsvurderinger = nyVilkårsvurdering()
        vilkårsvurderinger.samletUtfall() shouldBe Utfall.OPPFYLT
    }
}
