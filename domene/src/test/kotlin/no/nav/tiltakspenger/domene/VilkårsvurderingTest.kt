package no.nav.tiltakspenger.domene

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VilkårsvurderingTest {
    @Test
    fun `en vilkårsvurdering har utfall IKKE_VURDERT`() {
        val vilkårsvurdering = Vilkårsvurdering(vilkår = ErOver18År)
        assertEquals(Utfall.IKKE_VURDERT, vilkårsvurdering.utfall)
    }

    @Test
    fun `en vilkårsvurdering som mottar riktig fakta har utfall VURDERT_OG_OPPFYLT`() {
        val vilkårsvurdering = Vilkårsvurdering(vilkår = ErOver18År)
        assertEquals(Utfall.IKKE_VURDERT, vilkårsvurdering.utfall)

        val vurderingMedUtfall = vilkårsvurdering.vurder(AldersFaktum(fødselsdato = 12.april(2019), kilde = FaktumKilde.BRUKER))
        assertEquals(Utfall.VURDERT_OG_OPPFYLT, vurderingMedUtfall.utfall)
    }
}
