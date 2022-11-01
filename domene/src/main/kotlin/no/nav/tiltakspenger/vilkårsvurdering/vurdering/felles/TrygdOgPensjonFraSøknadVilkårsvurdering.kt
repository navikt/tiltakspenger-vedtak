package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

abstract class TrygdOgPensjonFraSøknadVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) : Vilkårsvurdering() {

    protected val søknadVurderinger: List<Vurdering> = lagVurderingerFraSøknad()

    private fun lagVurderingerFraSøknad(): List<Vurdering> =
        søknad.trygdOgPensjon
            .filter { Periode(it.fom ?: LocalDate.MIN, it.tom ?: LocalDate.MAX).overlapperMed(vurderingsperiode) }
            // TODO: Filter på utbetaler?
            .map {
                Vurdering(
                    vilkår = lovreferanse(),
                    kilde = SØKNADKILDE,
                    fom = it.fom,
                    tom = it.tom,
                    utfall = Utfall.IKKE_OPPFYLT,
                    detaljer = "${it.prosent} utbetaling fra ${it.utbetaler}",
                )
            }.ifEmpty {
                // TODO: Hva hvis det er oppgitt noe i søknaden, men det ikke har overlappende periode?
                listOf(
                    Vurdering(
                        vilkår = lovreferanse(),
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
