package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.IkkeImplementertVurdering

class SykepengerVilkårsvurdering(vurderingsperiode: Periode) : IkkeImplementertVurdering(vurderingsperiode) {
    override fun kilde() = "Infotrygd/Speil"

    override fun vilkår(): Vilkår = Vilkår.SYKEPENGER
}

class GjenlevendepensjonVilkårsvurdering(vurderingsperiode: Periode) : IkkeImplementertVurdering(vurderingsperiode) {
    override fun kilde() = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.GJENLEVENDEPENSJON
}

class SupplerendeStønadVilkårsvurdering(vurderingsperiode: Periode) : IkkeImplementertVurdering(vurderingsperiode) {
    override fun kilde() = "SupStønad"

    override fun vilkår(): Vilkår = Vilkår.SUPPLERENDESTØNAD
}

class AlderspensjonVilkårsvurdering(vurderingsperiode: Periode) : IkkeImplementertVurdering(vurderingsperiode) {
    override fun kilde() = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.ALDERSPENSJON
}
