package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.FraOgMedSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class AlderspensjonVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
) : Vilkårsvurdering() {

    override fun vilkår(): Vilkår = Vilkår.ALDERSPENSJON

    private val fraOgMedSpmVurdering = FraOgMedSpmVurdering(
        spm = søknad.alderspensjon,
        vilkår = vilkår(),
        vurderingsperiode = vurderingsperiode,
    )

    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet() = fraOgMedSpmVurdering.avgjørUtfall()

    override fun vurderinger(): List<Vurdering> =
        listOfNotNull(fraOgMedSpmVurdering.lagVurderingFraSøknad(), manuellVurdering)
}
