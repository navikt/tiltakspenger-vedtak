package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class AAPVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
) : StatligArenaYtelseVilkårsvurdering() {
    override val lovreferanse: Lovreferanse = Lovreferanse.AAP
    override var manuellVurdering: Vurdering? = null
    override val ytelseVurderinger: List<Vurdering> =
        lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.AA)
}
