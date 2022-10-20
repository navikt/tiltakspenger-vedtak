package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class AAPVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
    private val baseManuellOgAutomatiskVilkårsvurdering: BaseManuellOgAutomatiskVilkårsvurdering = BaseManuellOgAutomatiskVilkårsvurdering(
        automatiskVilkårsvurdering = BaseStatligYtelseVilkårsvurdering(
            ytelser = ytelser,
            vurderingsperiode = vurderingsperiode,
            type = YtelseSak.YtelseSakYtelsetype.AA,
        )
    )
) : IVilkårsvurdering by baseManuellOgAutomatiskVilkårsvurdering,
    IManuellVilkårsvurdering by baseManuellOgAutomatiskVilkårsvurdering,
    StatligYtelseVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse: Lovreferanse = Lovreferanse.AAP
}
