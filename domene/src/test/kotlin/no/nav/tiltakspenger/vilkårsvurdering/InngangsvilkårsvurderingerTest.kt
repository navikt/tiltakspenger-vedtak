package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.februarDateTime
import no.nav.tiltakspenger.objectmothers.ObjectMother.kvpJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyDagpengerVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyIntroprogrammetVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyKommunaleYtelserVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyKvpVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyStatligeYtelserVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.junit.jupiter.api.Test

class InngangsvilkårsvurderingerTest {

    @Test
    fun `en vilkårsvurdering med kvp skal gi manuell vurdering`() {
        val vilkårsvurderinger = nyVilkårsvurdering(
            kommunaleYtelserVilkårsvurderingKategori = nyKommunaleYtelserVilkårsvurdering(
                kvpVilkårsvurdering = nyKvpVilkårsvurdering(
                    søknad = nySøknadMedTiltak(
                        kvp = kvpJa(),
                    ),
                ),
            ),
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
                    søknad = nySøknadMedTiltak(
                        intro = periodeJa(),
                    ),
                ),
            ),
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        vilkårsvurderinger.vurderinger().ikkeOppfylte().size shouldBe 0
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
                        ),
                    ),
                ),
            ),
        )

        vilkårsvurderinger.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        vilkårsvurderinger.vurderinger().ikkeOppfylte().size shouldBe 0
    }

    @Test
    fun `en oppfylt vilkårsvurdering`() {
        val vilkårsvurderinger = nyVilkårsvurdering()
        vilkårsvurderinger.samletUtfall() shouldBe Utfall.OPPFYLT
    }
}
