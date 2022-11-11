package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType

abstract class TrygdOgPensjonFraSøknadVilkårsvurdering(
) : Vilkårsvurdering() {
    fun lagVurderingerFraSøknad(trygdOgPensjon: List<TrygdOgPensjon>): List<Vurdering> =
        trygdOgPensjon
            .map {
                Vurdering(
                    vilkår = vilkår(),
                    vurderingType = VurderingType.AUTOMATISK,
                    kilde = SØKNADKILDE,
                    fom = it.fom,
                    tom = it.tom,
                    utfall = Utfall.KREVER_MANUELL_VURDERING,
                    detaljer = detaljer(it.prosent, it.utbetaler),
                )
            }.ifEmpty {
                listOf(
                    Vurdering(
                        vilkår = vilkår(),
                        vurderingType = VurderingType.AUTOMATISK,
                        kilde = SØKNADKILDE,
                        fom = null,
                        tom = null,
                        utfall = Utfall.OPPFYLT,
                        detaljer = "",
                    )
                )
            }

    private fun detaljer(prosent: Int?, utbetaler: String): String {
        return if (prosent == null) {
            "Ukjent prosent utbetaling fra $utbetaler"
        } else {
            "$prosent prosent utbetaling fra $utbetaler"
        }
    }

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
    }
}
