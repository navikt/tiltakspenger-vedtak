package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.UføreVedtak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class UføreVilkarsvurdering(
    private val uføreVedtak: UføreVedtak,
    private val vurderingsperiode: Periode,

    ) : Vilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.UFØRETRYGD

    override fun vurderinger(): List<Vurdering> {
        TODO("Not yet implemented")
    }

    override fun detIkkeManuelleUtfallet(): Utfall {
        TODO("Not yet implemented")
    }
}
