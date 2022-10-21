package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class DagpengerVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
    private val manuellOgAutomatiskVilkårsvurdering: KomplettManuellOgAutomatiskVilkårsvurderingKomponent =
        KomplettManuellOgAutomatiskVilkårsvurderingKomponent(
            automatiskVilkårsvurdering = AutomatiskDagpengerVilkårsvurdering(
                ytelser = ytelser,
                vurderingsperiode = vurderingsperiode
            )
        )
) : IKomplettVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    StatligYtelseVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse: Lovreferanse = Lovreferanse.DAGPENGER

    class AutomatiskDagpengerVilkårsvurdering(ytelser: List<YtelseSak>, vurderingsperiode: Periode) :
        StatligYtelseVilkårsvurderingKomponent(
            ytelser = ytelser,
            vurderingsperiode = vurderingsperiode,
            type = YtelseSak.YtelseSakYtelsetype.DAGP
        )
}
