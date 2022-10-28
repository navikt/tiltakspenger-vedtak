package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad

// TODO: Logikken her må kvalitetssikres
class PensjonsinntektVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) : TrygdOgPensjonFraSøknadVilkårsvurdering(søknad, vurderingsperiode) {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.PENSJONSINNTEKT

    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = søknadVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    override fun vurderinger(): List<Vurdering> =
        (søknadVurderinger + manuellVurdering).filterNotNull()
}
