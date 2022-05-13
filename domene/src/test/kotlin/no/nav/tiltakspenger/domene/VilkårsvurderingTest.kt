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

    @Test
    fun `en vilkårsvurdering om KVP hvor bruker sier hen går på det skal til manuell behandling`() {
        val vilkårsvurdering = Vilkårsvurdering(vilkår  = KVP)
        val vurderingMedUtfall = vilkårsvurdering.vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
        assertEquals(Utfall.VURDERT_OG_TRENGER_MANUELL_VURDERING, vurderingMedUtfall.utfall)
    }

    @Test
    fun `en KVP vilkårsvurdering med fakta fra bruker og fakta fra saksbehandler skal avgjøres ved holmgang`() {
        val vilkårsvurdering = Vilkårsvurdering(vilkår  = KVP)
        val vurderingMedUtfall = vilkårsvurdering
            .vurder(KVPFaktum(deltarKVP = true, kilde = FaktumKilde.BRUKER))
            .vurder(KVPFaktum(deltarKVP = false, kilde = FaktumKilde.SAKSBEHANDLER))
        assertEquals(Utfall.VURDERT_OG_OPPFYLT, vurderingMedUtfall.utfall)
    }
}
