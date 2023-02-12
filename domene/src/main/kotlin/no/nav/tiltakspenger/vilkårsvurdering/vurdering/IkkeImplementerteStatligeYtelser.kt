package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.IkkeImplementertVurdering

class SykepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Infotrygd/Speil"

    override fun vilkår(): Vilkår = Vilkår.SYKEPENGER
}

// class UføretrygdVilkårsvurdering : IkkeImplementertVurdering() {
//    override fun kilde() = "Pesys"
//
//    override fun vilkår(): Vilkår = Vilkår.UFØRETRYGD
// }

class OvergangsstønadVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "[ny løsning]"

    override fun vilkår(): Vilkår = Vilkår.OVERGANGSSTØNAD
}

class GjenlevendepensjonVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.GJENLEVENDEPENSJON
}

class SupplerendeStønadVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "SupStønad"

    override fun vilkår(): Vilkår = Vilkår.SUPPLERENDESTØNAD
}

class AlderspensjonVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.ALDERSPENSJON
}
