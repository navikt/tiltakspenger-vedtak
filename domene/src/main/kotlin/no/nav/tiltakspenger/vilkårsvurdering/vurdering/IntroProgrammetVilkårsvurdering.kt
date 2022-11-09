package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering

class IntroProgrammetVilkårsvurdering(
    override var vurderinger: List<Vurdering> = emptyList(),
) : KommunalYtelseVilkårsvurdering(vurderinger) {
    override fun vilkår() = Vilkår.INTROPROGRAMMET

    fun leggTilSøknad(søknad: Søknad): IntroProgrammetVilkårsvurdering {
        vurderinger += lagVurderingFraSøknad(søknad)
        return this
    }

    private fun lagVurderingFraSøknad(søknad: Søknad) = Vurdering(
        vilkår = vilkår(),
        vurderingType = VurderingType.AUTOMATISK,
        kilde = KILDE,
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(søknad),
        detaljer = "",
    )

    fun avgjørUtfall(søknad: Søknad): Utfall =
        if (!søknad.deltarIntroduksjonsprogrammet) Utfall.OPPFYLT else Utfall.KREVER_MANUELL_VURDERING
}
