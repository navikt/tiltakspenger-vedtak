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
                    detaljer = "${it.prosent} utbetaling fra ${it.utbetaler}",
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

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
    }
}
