package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering
import java.time.LocalDate

class IntroProgrammetVilkårsvurdering(søknad: Søknad, vurderingsperiode: Periode) :
    KommunalYtelseVilkårsvurdering(søknad, vurderingsperiode) {

    override fun lagVurderingFraSøknad() = Vurdering(
        vilkår = lovreferanse(),
        kilde = KILDE,
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(),
        detaljer = "",
    )

    override fun avgjørUtfall(): Utfall {
        if (!søknad.deltarIntroduksjonsprogrammet) return Utfall.OPPFYLT
        val tom = søknad.introduksjonsprogrammetDetaljer?.tom ?: LocalDate.MAX
        return if (vurderingsperiode.overlapperMed(Periode(søknad.introduksjonsprogrammetDetaljer!!.fom, tom))) {
            Utfall.IKKE_OPPFYLT
        } else {
            Utfall.OPPFYLT
        }
    }

    override fun lovreferanse() = Vilkår.INTROPROGRAMMET
}
