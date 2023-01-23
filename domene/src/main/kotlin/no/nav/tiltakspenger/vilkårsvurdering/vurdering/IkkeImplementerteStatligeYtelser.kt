package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.IkkeImplementertVurdering

class SykepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Infotrygd/Speil"

    override fun vilkår(): Vilkår = Vilkår.SYKEPENGER
}

class UføretrygdVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.UFØRETRYGD
}

class OvergangsstønadVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "[ny løsning]"

    override fun vilkår(): Vilkår = Vilkår.OVERGANGSSTØNAD
}

class PleiepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun vilkår(): Vilkår = Vilkår.PLEIEPENGER
}

class ForeldrepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "FP-Sak"

    override fun vilkår(): Vilkår = Vilkår.FORELDREPENGER
}

class SvangerskapspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "FP-Sak"

    override fun vilkår(): Vilkår = Vilkår.SVANGERSKAPSPENGER
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

class OpplæringspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun vilkår(): Vilkår = Vilkår.OPPLÆRINGSPENGER
}

class OmsorgspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun vilkår(): Vilkår = Vilkår.OMSORGSPENGER
}
