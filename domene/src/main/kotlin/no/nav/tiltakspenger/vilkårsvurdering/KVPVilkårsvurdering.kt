package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad

class KVPVilkårsvurdering(søknad: Søknad, vurderingsperiode: Periode) :
    KommunalYtelseVilkårsvurdering(søknad, vurderingsperiode) {

    override fun lagVurderingFraSøknad() = Vurdering(
        lovreferanse = lovreferanse(),
        kilde = KILDE,
        fom = null,
        tom = null,
        utfall = avgjørUtfall(),
        detaljer = "",
    )

    override fun avgjørUtfall() = if (søknad.deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT

    override fun lovreferanse() = Lovreferanse.KVP
}
