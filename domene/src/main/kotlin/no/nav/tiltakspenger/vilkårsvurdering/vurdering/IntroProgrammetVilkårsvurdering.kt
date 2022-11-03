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
        vilkår = vilkår(),
        kilde = KILDE,
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(),
        detaljer = "",
    )

    override fun avgjørUtfall(): Utfall {
        if (!søknad.deltarIntroduksjonsprogrammet) return Utfall.OPPFYLT
        if (søknad.introduksjonsprogrammetDetaljer == null) {
            //Dette skal vel ikke skje
            return Utfall.KREVER_MANUELL_VURDERING
        }
        val søknadsperiode = Periode(
            søknad.introduksjonsprogrammetDetaljer.fom,
            søknad.introduksjonsprogrammetDetaljer.tom ?: LocalDate.MAX
        )
        return if (vurderingsperiode.overlapperMed(søknadsperiode)) {
            Utfall.IKKE_OPPFYLT
        } else {
            Utfall.OPPFYLT
        }
    }

    override fun vilkår() = Vilkår.INTROPROGRAMMET
}
