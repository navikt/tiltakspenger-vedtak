package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.TrygdOgPensjonFraSøknadVilkårsvurdering

class PensjonsinntektVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) : TrygdOgPensjonFraSøknadVilkårsvurdering(søknad, vurderingsperiode) {
    override fun vilkår(): Vilkår = Vilkår.PENSJONSINNTEKT

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
