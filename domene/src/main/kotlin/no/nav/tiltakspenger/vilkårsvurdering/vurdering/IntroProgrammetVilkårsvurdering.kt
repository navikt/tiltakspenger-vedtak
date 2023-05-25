package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.UklartPeriodeSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class IntroProgrammetVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
) : Vilkårsvurdering() {

    override fun vilkår() = Vilkår.INTROPROGRAMMET

    private val periodeSpmVurdering = UklartPeriodeSpmVurdering(
        spm = søknad.intro,
        søknadVersjon = søknad.versjon,
        vilkår = vilkår(),
        vurderingsperiode = vurderingsperiode,
    )

    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet() = periodeSpmVurdering.avgjørUtfall()

    override fun vurderinger(): List<Vurdering> =
        listOfNotNull(periodeSpmVurdering.lagVurderingFraSøknad(), manuellVurdering)
}
