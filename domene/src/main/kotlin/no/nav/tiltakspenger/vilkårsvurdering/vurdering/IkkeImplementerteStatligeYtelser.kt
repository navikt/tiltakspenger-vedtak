package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering

class SykepengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "Infotrygd/Speil"

    override fun vilkår(): Vilkår = Vilkår.SYKEPENGER

    fun leggTilIkkeImplementert(): SykepengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}

class UføretrygdVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.UFØRETRYGD

    fun leggTilIkkeImplementert(): UføretrygdVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}

class OvergangsstønadVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "[ny løsning]"

    override fun vilkår(): Vilkår = Vilkår.OVERGANGSSTØNAD

    fun leggTilIkkeImplementert(): OvergangsstønadVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}

class PleiepengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "K9"

    override fun vilkår(): Vilkår = Vilkår.PLEIEPENGER

    fun leggTilIkkeImplementert(): PleiepengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}

class ForeldrepengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "FP-Sak"

    override fun vilkår(): Vilkår = Vilkår.FORELDREPENGER

    fun leggTilIkkeImplementert(): ForeldrepengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}


class SvangerskapspengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "FP-Sak"

    override fun vilkår(): Vilkår = Vilkår.SVANGERSKAPSPENGER

    fun leggTilIkkeImplementert(): SvangerskapspengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}


class GjenlevendepensjonVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.GJENLEVENDEPENSJON

    fun leggTilIkkeImplementert(): GjenlevendepensjonVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}


class SupplerendeStønadVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "SupStønad"

    override fun vilkår(): Vilkår = Vilkår.SUPPLERENDESTØNAD

    fun leggTilIkkeImplementert(): SupplerendeStønadVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}


class AlderspensjonVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "Pesys"

    override fun vilkår(): Vilkår = Vilkår.ALDERSPENSJON

    fun leggTilIkkeImplementert(): AlderspensjonVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}


class OpplæringspengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "K9"

    override fun vilkår(): Vilkår = Vilkår.OPPLÆRINGSPENGER

    fun leggTilIkkeImplementert(): OpplæringspengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}

class OmsorgspengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    val kilde = "K9"

    override fun vilkår(): Vilkår = Vilkår.OMSORGSPENGER

    fun leggTilIkkeImplementert(): OmsorgspengerVilkårsvurdering {
        vurderinger += ikkeImplementertVurdering(kilde)
        return this
    }
}
