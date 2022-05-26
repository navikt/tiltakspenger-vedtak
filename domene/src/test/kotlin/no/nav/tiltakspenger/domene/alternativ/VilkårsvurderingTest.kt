package no.nav.tiltakspenger.domene.alternativ

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.Utfall
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class VilkårsvurderingTest {

    @Test
    fun test1() {
        val periode = Periode(LocalDate.now().minusDays(2), LocalDate.now())
        val vilkårsvurdering = Over18Vilkårsvurdering()
        vilkårsvurdering.fyllInnFaktumDerDetPasser(KVPFaktum(true))
        assertEquals(Utfall.IkkeVurdert, vilkårsvurdering.vurder(periode).utfallsperioder.first().utfall)

        vilkårsvurdering.fyllInnFaktumDerDetPasser(Over18Faktum(LocalDate.now().minusYears(20)))
        assertEquals(Utfall.VurdertOgOppfylt, vilkårsvurdering.vurder(periode).utfallsperioder.first().utfall)
    }
}