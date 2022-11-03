package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

abstract class TrygdOgPensjonFraSøknadVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) : Vilkårsvurdering() {

    protected val søknadVurderinger: List<Vurdering> = lagVurderingerFraSøknad()

    private fun lagVurderingerFraSøknad(): List<Vurdering> =
        søknad.trygdOgPensjon
            .map {
                Vurdering(
                    vilkår = vilkår(),
                    kilde = SØKNADKILDE,
                    fom = it.fom,
                    tom = it.tom,
                    utfall = Utfall.KREVER_MANUELL_VURDERING,
                    detaljer = "${it.prosent} utbetaling fra ${it.utbetaler}",
                )
            }.ifEmpty {
                listOf(
                    Vurdering(
                        vilkår = vilkår(),
                        kilde = SØKNADKILDE,
                        fom = null,
                        tom = null,
                        utfall = Utfall.OPPFYLT,
                        detaljer = "",
                    )
                )
            }

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
    }
}
