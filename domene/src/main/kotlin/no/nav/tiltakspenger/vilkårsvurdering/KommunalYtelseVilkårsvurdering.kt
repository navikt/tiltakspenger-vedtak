package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad

sealed class KommunalYtelseVilkårsvurdering(
    protected val søknad: Søknad,
    protected val vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    companion object {
        const val KILDE = "Søknad"
    }

    private val søknadVurdering = this.lagVurderingFraSøknad()
    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet() = søknadVurdering.utfall

    protected abstract fun lagVurderingFraSøknad(): Vurdering

    protected abstract fun avgjørUtfall(): Utfall

    override fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)
}
