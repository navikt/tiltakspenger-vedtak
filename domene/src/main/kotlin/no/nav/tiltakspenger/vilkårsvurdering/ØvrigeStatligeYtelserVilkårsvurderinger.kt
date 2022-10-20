package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode

class SykepengerVilkårsvurdering(
    private val vurderingsperiode: Periode,
    private val manuellVilkårsvurdering: KunManuellVilkårsvurderinger
) : IVilkårsvurdering by manuellVilkårsvurdering,
    IManuellVilkårsvurdering by manuellVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.KVP
}

class UføretrygdVilkårsvurdering(
    private val vurderingsperiode: Periode,
    private val manuellVilkårsvurdering: KunManuellVilkårsvurderinger
) : IVilkårsvurdering by manuellVilkårsvurdering,
    IManuellVilkårsvurdering by manuellVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.KVP
}
