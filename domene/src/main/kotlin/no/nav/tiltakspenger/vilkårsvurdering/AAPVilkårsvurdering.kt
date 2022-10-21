package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class AAPVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
    private val manuellOgAutomatiskVilkårsvurdering: KomplettManuellOgAutomatiskVilkårsvurderingKomponent = KomplettManuellOgAutomatiskVilkårsvurderingKomponent(
        automatiskVilkårsvurdering = StatligYtelseVilkårsvurderingKomponent(
            ytelser = ytelser,
            vurderingsperiode = vurderingsperiode,
            type = YtelseSak.YtelseSakYtelsetype.AA,
        )
    )
) : IKomplettVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    StatligYtelseVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse: Lovreferanse = Lovreferanse.AAP
}
