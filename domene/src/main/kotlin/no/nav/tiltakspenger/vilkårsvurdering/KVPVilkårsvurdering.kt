package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad

class KVPVilkårsvurdering(
    søknad: Søknad,
    vurderingsperiode: Periode,
    baseManuellOgAutomatiskVilkårsvurdering: BaseManuellOgAutomatiskVilkårsvurdering = BaseManuellOgAutomatiskVilkårsvurdering(
        automatiskVilkårsvurdering = AutomatiskKVPVilkårsvurdering(søknad, vurderingsperiode)
    )
) : IVilkårsvurdering by baseManuellOgAutomatiskVilkårsvurdering,
    IManuellVilkårsvurdering by baseManuellOgAutomatiskVilkårsvurdering,
    ILovreferanse,
    Vilkårsvurdering() {

    override val lovreferanse = Lovreferanse.KVP

    class AutomatiskKVPVilkårsvurdering(private val søknad: Søknad, private val vurderingsperiode: Periode) :
        IAutomatiskVilkårsvurdering {
        override fun vurderinger(): List<Vurdering> = listOf(
            Vurdering(
                kilde = KILDE,
                fom = null,
                tom = null,
                utfall = avgjørUtfall(),
                detaljer = "",
            )
        )

        private fun avgjørUtfall() = if (søknad.deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT

        override fun detIkkeManuelleUtfallet(): Utfall = avgjørUtfall()
    }

    companion object {
        private const val KILDE: String = "Søknad"
    }
}
