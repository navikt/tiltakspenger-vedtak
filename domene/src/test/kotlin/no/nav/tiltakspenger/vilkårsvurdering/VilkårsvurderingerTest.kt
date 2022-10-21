package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import org.junit.jupiter.api.Test

class VilkårsvurderingerTest {

    @Test
    fun `man skal på toppnivå kunne se alle ikke-oppfylte vilkår`() {

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

    }
}
