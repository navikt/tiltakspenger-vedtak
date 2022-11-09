package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering

class KVPVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : KommunalYtelseVilkårsvurdering(vurderinger) {
    override fun vilkår() = Vilkår.KVP

    fun leggTilSøknad(søknad: Søknad): KVPVilkårsvurdering {
        vurderinger += lagVurderingFraSøknad(søknad)
        return this
    }

    private fun lagVurderingFraSøknad(søknad: Søknad) = Vurdering(
        vilkår = vilkår(),
        vurderingType = VurderingType.AUTOMATISK,
        kilde = KILDE,
        fom = null,
        tom = null,
        utfall = if (søknad.deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT,
        detaljer = "",
    )
}
