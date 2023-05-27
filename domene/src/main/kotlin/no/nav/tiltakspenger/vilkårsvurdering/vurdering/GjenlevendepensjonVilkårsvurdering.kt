package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.FraOgMedSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class GjenlevendepensjonVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
) : Vilkårsvurdering() {

    override fun vilkår(): Vilkår = Vilkår.GJENLEVENDEPENSJON

    private val fraOgMedSpmVurdering = FraOgMedSpmVurdering(
        spm = søknad.gjenlevendepensjon,
        vilkår = vilkår(),
        vurderingsperiode = vurderingsperiode,
    )

    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet() = fraOgMedSpmVurdering.avgjørUtfall()

    override fun vurderinger(): List<Vurdering> =
        listOfNotNull(fraOgMedSpmVurdering.lagVurderingFraSøknad(), manuellVurdering)
}
