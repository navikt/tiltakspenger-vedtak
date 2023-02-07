package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak.Ytelser.FORELDREPENGER
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligFPogK9YtelseVilkårsvurdering

class ForeldrepengerVilkårsvurdering(
    ytelser: List<ForeldrepengerVedtak>,
    vurderingsperiode: Periode,
) : StatligFPogK9YtelseVilkårsvurdering(ytelser, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.FORELDREPENGER
    override fun ytelseType() = listOf(FORELDREPENGER)
    override fun kilde() = "FPSAK"
}
