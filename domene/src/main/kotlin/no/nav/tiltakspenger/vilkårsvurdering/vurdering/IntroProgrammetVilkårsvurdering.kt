package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.IntroduksjonsprogrammetDetaljer
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.KommunalYtelseVilkårsvurdering
import java.time.LocalDate

class IntroProgrammetVilkårsvurdering(
    val deltarIntroduksjonsprogrammet: Boolean,
    val introduksjonsprogrammetDetaljer: IntroduksjonsprogrammetDetaljer?,
    vurderingsperiode: Periode
) :
    KommunalYtelseVilkårsvurdering(vurderingsperiode) {

    override fun lagVurderingFraSøknad() = Vurdering(
        vilkår = vilkår(),
        kilde = KILDE,
        fom = introduksjonsprogrammetDetaljer?.fom,
        tom = introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(),
        detaljer = "",
    )

    override fun avgjørUtfall(): Utfall {
        if (!deltarIntroduksjonsprogrammet) return Utfall.OPPFYLT
        if (introduksjonsprogrammetDetaljer == null) {
            //Dette skal vel ikke skje
            return Utfall.KREVER_MANUELL_VURDERING
        }
        val søknadsperiode = Periode(
            fra = introduksjonsprogrammetDetaljer.fom,
            til = introduksjonsprogrammetDetaljer.tom ?: LocalDate.MAX
        )
        return if (vurderingsperiode.overlapperMed(søknadsperiode)) {
            Utfall.IKKE_OPPFYLT
        } else {
            Utfall.OPPFYLT
        }
    }

    override fun vilkår() = Vilkår.INTROPROGRAMMET
}
