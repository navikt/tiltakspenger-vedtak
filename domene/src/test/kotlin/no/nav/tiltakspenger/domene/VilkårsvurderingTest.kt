package no.nav.tiltakspenger.domene

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VilkårsvurderingTest {
    @Test
    fun `en vilkårsvurdering har utfall IKKE_VURDERT`() {
        val vilkårsvurdering = Vilkårsvurdering(vilkår = ErOver18År)
        assertEquals(Utfall.IKKE_VURDERT, vilkårsvurdering.utfall)

        vilkårsvurdering.vurder(AldersFaktum(fødselsdato = 12.april(2019), kilde = FaktumKilde.BRUKER))
//        assertEquals(Utfall.VURDERT_OG_OPPFYLT, vilkårsvurdering.utfall)
    }
}