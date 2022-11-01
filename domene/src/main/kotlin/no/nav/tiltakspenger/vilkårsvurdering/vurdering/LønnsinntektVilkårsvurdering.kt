package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Lovreferanse
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.TrygdOgPensjonFraSøknadVilkårsvurdering

// TODO: Logikken her må kvalitetssikres
class LønnsinntektVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode
) : TrygdOgPensjonFraSøknadVilkårsvurdering(søknad, vurderingsperiode) {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.LØNNSINNTEKT

    private val aInntektVurderinger: List<Vurdering> = lagVurderingerFraAInntekt()
    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = aInntektVurderinger.map { it.utfall } + søknadVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    private fun lagVurderingerFraAInntekt(): List<Vurdering> =
        listOf(
            Vurdering(
                lovreferanse = lovreferanse(),
                kilde = AINNTEKTKILDE,
                fom = null,
                tom = null,
                utfall = Utfall.IKKE_IMPLEMENTERT,
                detaljer = "",
            )
        )


    override fun vurderinger(): List<Vurdering> =
        (aInntektVurderinger + søknadVurderinger + manuellVurdering).filterNotNull()

    companion object {
        private const val AINNTEKTKILDE = "A-Inntekt"
    }
}
