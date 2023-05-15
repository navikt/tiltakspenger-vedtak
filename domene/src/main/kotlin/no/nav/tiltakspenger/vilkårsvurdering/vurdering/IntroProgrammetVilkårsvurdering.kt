package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
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
        fom = søknad.intro.periode?.fra,
        tom = søknad.intro.periode?.til,
        utfall = avgjørUtfall(),
        detaljer = detaljer(),
    )

    private fun detaljer(): String =
        when (søknad.intro.deltar) {
            true -> "Svart JA i søknaden"
            false -> "Svart NEI i søknaden"
//            else -> "Ikke relevant"
        }

    override fun avgjørUtfall(): Utfall =
        when (søknad.intro.deltar) {
            true -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }

    override fun vilkår() = Vilkår.INTROPROGRAMMET
}
