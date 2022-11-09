package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.TrygdOgPensjonFraSøknadVilkårsvurdering

class LønnsinntektVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : TrygdOgPensjonFraSøknadVilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.LØNNSINNTEKT

    // Her må vi få inn fakta og kalle denne f.eks
    // fun leggTilFakta(ainntekt: AInntekt): LønnsinntektVilkårsvurdering {
    fun leggTilFakta(): LønnsinntektVilkårsvurdering {
        vurderinger += lagVurderingerFraAInntekt()
        return this
    }

    fun leggTilSøknad(søknad: Søknad): LønnsinntektVilkårsvurdering {
        vurderinger = lagVurderingerFraSøknad(søknad.trygdOgPensjon)
        return this
    }

    private fun lagVurderingerFraAInntekt(): List<Vurdering> =
        listOf(
            Vurdering(
                vilkår = vilkår(),
                vurderingType = VurderingType.AUTOMATISK,
                kilde = AINNTEKTKILDE,
                fom = null,
                tom = null,
                utfall = Utfall.IKKE_IMPLEMENTERT,
                detaljer = "",
            )
        )

    companion object {
        private const val AINNTEKTKILDE = "A-Inntekt"
    }
}
