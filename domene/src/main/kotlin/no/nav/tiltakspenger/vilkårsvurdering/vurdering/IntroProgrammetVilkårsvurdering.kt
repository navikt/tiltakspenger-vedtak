package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering

class IntroProgrammetVilkårsvurdering(søknad: Søknad, vurderingsperiode: Periode) :
    KommunalYtelseVilkårsvurdering(søknad, vurderingsperiode) {

    override fun lagVurderingFraSøknad() = Vurdering(
        vilkår = vilkår(),
        kilde = KILDE,
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(),
        detaljer = detaljer(),
    )

    private fun detaljer(): String =
        when (søknad.deltarIntroduksjonsprogrammet) {
            true -> "Svart JA i søknaden"
            false -> "Svart NEI i søknaden"
            else -> "Ikke relevant"
        }

    override fun avgjørUtfall(): Utfall =
        when (søknad.deltarIntroduksjonsprogrammet) {
            true -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }

    override fun vilkår() = Vilkår.INTROPROGRAMMET
}
