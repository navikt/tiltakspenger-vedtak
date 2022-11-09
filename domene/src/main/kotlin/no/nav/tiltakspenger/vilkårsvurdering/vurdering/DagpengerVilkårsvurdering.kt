package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering

class DagpengerVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : StatligArenaYtelseVilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.DAGPENGER

    fun leggTilFakta(ytelser: List<YtelseSak>, vurderingsperiode: Periode): DagpengerVilkårsvurdering {
        vurderinger = lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.DAGP)
        return this
    }
}
