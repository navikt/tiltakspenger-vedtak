package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.TrygdOgPensjonFraSøknadVilkårsvurdering

class PensjonsinntektVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : TrygdOgPensjonFraSøknadVilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.PENSJONSINNTEKT

    fun leggTilSøknad(søknad: Søknad): PensjonsinntektVilkårsvurdering {
        vurderinger = lagVurderingerFraSøknad(søknad.trygdOgPensjon)
        return this
    }
}
