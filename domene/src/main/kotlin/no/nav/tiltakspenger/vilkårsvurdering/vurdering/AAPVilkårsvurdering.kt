package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligArenaYtelseVilkårsvurdering

class AAPVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
) : StatligArenaYtelseVilkårsvurdering() {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.AAP
    override var manuellVurdering: Vurdering? = null
    override val ytelseVurderinger: List<Vurdering> =
        lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.AA)
}
