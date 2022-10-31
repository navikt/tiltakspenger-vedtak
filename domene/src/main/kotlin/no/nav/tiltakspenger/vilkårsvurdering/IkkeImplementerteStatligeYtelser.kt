package no.nav.tiltakspenger.vilkårsvurdering

class SykepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Infotrygd/Speil"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.SYKEPENGER
}

class UføretrygdVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.UFØRETRYGD
}

class OvergangsstønadVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "[ny løsning]"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.OVERGANGSSTØNAD
}

class PleiepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.PLEIEPENGER
}

class ForeldrepengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "FP-Sak"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.FORELDREPENGER
}


class SvangerskapspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "FP-Sak"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.SVANGERSKAPSPENGER
}


class GjenlevendepensjonVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.GJENLEVENDEPENSJON
}


class SupplerendeStønadVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "SupStønad"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.SUPPLERENDESTØNAD
}


class AlderspensjonVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "Pesys"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.ALDERSPENSJON
}


class OpplæringspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.OPPLÆRINGSPENGER
}

class OmsorgspengerVilkårsvurdering : IkkeImplementertVurdering() {
    override fun kilde() = "K9"

    override fun lovreferanse(): Lovreferanse = Lovreferanse.OMSORGSPENGER
}
