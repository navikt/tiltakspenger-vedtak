package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering

class KVPVilkårsvurdering(val deltarKvp: Boolean, vurderingsperiode: Periode) :
    KommunalYtelseVilkårsvurdering(vurderingsperiode) {

    override fun lagVurderingFraSøknad() = Vurdering(
        vilkår = vilkår(),
        kilde = KILDE,
        fom = null,
        tom = null,
        utfall = avgjørUtfall(),
        detaljer = "",
    )

    override fun avgjørUtfall() = if (deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT

    override fun vilkår() = Vilkår.KVP
}
