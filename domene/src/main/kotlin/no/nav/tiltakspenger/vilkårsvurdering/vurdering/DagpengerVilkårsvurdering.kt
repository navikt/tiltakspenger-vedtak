package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering

class DagpengerVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
) : StatligArenaYtelseVilkårsvurdering(ytelser, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.DAGPENGER
    override fun ytelseType() = YtelseSak.YtelseSakYtelsetype.DAGP
}
