package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.FraOgMedSpmVurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

class PrivatPensjonsinntektVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.PENSJONSINNTEKT

    private val fraOgMedSpmVurdering = FraOgMedSpmVurdering(
        spm = søknad.trygdOgPensjon,
        vilkår = vilkår(),
        vurderingsperiode = vurderingsperiode,
    )
    override var manuellVurdering: Vurdering? = null

    fun lagVurderingFraSøknad() = fraOgMedSpmVurdering.lagVurderingFraSøknad()

    override fun vurderinger(): List<Vurdering> = listOfNotNull(lagVurderingFraSøknad(), manuellVurdering)
    override fun detIkkeManuelleUtfallet(): Utfall = fraOgMedSpmVurdering.avgjørUtfall()
}
