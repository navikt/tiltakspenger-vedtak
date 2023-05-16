package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.PeriodeSpmVurdering


class KVPVilkårsvurdering(søknad: Søknad, vurderingsperiode: Periode) :
    KommunalYtelseVilkårsvurdering(søknad, vurderingsperiode) {

    private val periodeSpmVurdering = PeriodeSpmVurdering(
        spm = søknad.kvp,
        søknadVersjon = søknad.versjon,
        vilkår = vilkår(),
    )

    override fun lagVurderingFraSøknad() = periodeSpmVurdering.lagVurderingFraSøknad()

    override fun avgjørUtfall() = periodeSpmVurdering.avgjørUtfall()

    override fun vilkår() = Vilkår.KVP

}
