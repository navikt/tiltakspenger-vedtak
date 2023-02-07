package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligFPogK9YtelseVilkårsvurdering

class PleiepengerVilkårsvurdering(
    ytelser: List<ForeldrepengerVedtak>,
    vurderingsperiode: Periode,
) : StatligFPogK9YtelseVilkårsvurdering(ytelser, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.PLEIEPENGER
    override fun ytelseType() = listOf(PLEIEPENGER_NÆRSTÅENDE, PLEIEPENGER_SYKT_BARN)
    override fun kilde() = "K9SAK"
}
