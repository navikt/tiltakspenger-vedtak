package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering

class AAPVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
) : StatligArenaYtelseVilkårsvurdering(ytelser, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.AAP
    override fun ytelseType() = YtelseSak.YtelseSakYtelsetype.AA
}
