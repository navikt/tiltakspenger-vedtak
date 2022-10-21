package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

class IntroProgrammetVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
    manuellOgAutomatiskVilkårsvurdering: KomplettManuellOgAutomatiskVilkårsvurderingKomponent = KomplettManuellOgAutomatiskVilkårsvurderingKomponent(
        automatiskVilkårsvurdering = AutomatiskIntroProgrammetVilkårsvurdering(
            søknad,
            vurderingsperiode
        )
    )
) : IKomplettVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellOgAutomatiskVilkårsvurdering,
    KommunalYtelseVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.INTROPROGRAMMET

    class AutomatiskIntroProgrammetVilkårsvurdering(
        private val søknad: Søknad,
        private val vurderingsperiode: Periode
    ) :
        IDelvisAutomatiskVilkårsvurdering {
        override fun vurderinger(): List<Vurdering> =
            listOf(
                Vurdering(
                    kilde = KILDE,
                    fom = søknad.introduksjonsprogrammetDetaljer?.fom,
                    tom = søknad.introduksjonsprogrammetDetaljer?.tom,
                    utfall = avgjørUtfall(),
                    detaljer = "",
                )
            )

        override fun detIkkeManuelleUtfallet(): Utfall = avgjørUtfall()

        private fun avgjørUtfall(): Utfall {
            if (!søknad.deltarIntroduksjonsprogrammet) return Utfall.OPPFYLT
            val tom = søknad.introduksjonsprogrammetDetaljer?.tom ?: LocalDate.MAX
            return if (vurderingsperiode.overlapperMed(Periode(søknad.introduksjonsprogrammetDetaljer!!.fom, tom))) {
                Utfall.IKKE_OPPFYLT
            } else {
                Utfall.OPPFYLT
            }
        }
    }

    companion object {
        private const val KILDE: String = "Søknad"
    }
}
