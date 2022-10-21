package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode

class SykepengerVilkårsvurdering(
    private val vurderingsperiode: Periode,
    private val manuellVilkårsvurdering: KomplettManuellVilkårsvurderingerKomponent
) : IKomplettVilkårsvurdering by manuellVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.KVP
}

class UføretrygdVilkårsvurdering(
    private val vurderingsperiode: Periode,
    private val manuellVilkårsvurdering: KomplettManuellVilkårsvurderingerKomponent
) : IKomplettVilkårsvurdering by manuellVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.KVP
}
