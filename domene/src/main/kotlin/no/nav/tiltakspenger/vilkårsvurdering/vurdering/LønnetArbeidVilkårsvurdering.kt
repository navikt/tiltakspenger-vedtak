package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.JaNeiSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class LønnetArbeidVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.LØNNSINNTEKT

    private val jaNeiSpmVurdering = JaNeiSpmVurdering(
        spm = søknad.lønnetArbeid,
        vilkår = vilkår(),
        vurderingsperiode = vurderingsperiode,
    )
    override var manuellVurdering: Vurdering? = null

    fun lagVurderingFraSøknad() = jaNeiSpmVurdering.lagVurderingFraSøknad()

    override fun vurderinger(): List<Vurdering> = listOfNotNull(lagVurderingFraSøknad(), manuellVurdering)
    override fun detIkkeManuelleUtfallet(): Utfall = jaNeiSpmVurdering.avgjørUtfall()
}
