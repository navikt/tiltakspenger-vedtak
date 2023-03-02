package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.StatligFPogK9YtelseVilkårsvurdering

class PleiepengerNærståendeVilkårsvurdering(
    ytelser: List<ForeldrepengerVedtak>,
    vurderingsperiode: Periode,
) : StatligFPogK9YtelseVilkårsvurdering(ytelser, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.PLEIEPENGER_NÆRSTÅENDE
    override fun ytelseType() = listOf(PLEIEPENGER_NÆRSTÅENDE)
    override fun kilde() = "K9SAK"
}
