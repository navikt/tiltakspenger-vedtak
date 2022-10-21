package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class DagpengerVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
) : StatligArenaYtelseVilkårsvurdering() {
    override val lovreferanse: Lovreferanse = Lovreferanse.DAGPENGER
    override var manuellVurdering: Vurdering? = null
    override val ytelseVurderinger: List<Vurdering> =
        lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.DAGP)
}
