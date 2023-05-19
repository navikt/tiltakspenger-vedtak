package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.UklartPeriodeSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class KVPVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
) : Vilkårsvurdering() {

    override fun vilkår() = Vilkår.KVP

    private val periodeSpmVurdering = UklartPeriodeSpmVurdering(
        spm = søknad.kvp,
        søknadVersjon = søknad.versjon,
        vilkår = vilkår(),
    )

    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet() = periodeSpmVurdering.avgjørUtfall()

    override fun vurderinger(): List<Vurdering> =
        listOfNotNull(periodeSpmVurdering.lagVurderingFraSøknad(), manuellVurdering)
}
