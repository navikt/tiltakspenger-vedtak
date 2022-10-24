package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

// TODO: Logikken her må kvalitetssikres
class PensjonsinntektVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) : Vilkårsvurdering() {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.PENSJONSINNTEKT

    private val søknadVurderinger: List<Vurdering> = lagVurderingerFraSøknad()
    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = søknadVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }
    
    private fun lagVurderingerFraSøknad(): List<Vurdering> =
        søknad.trygdOgPensjon
            .filter { Periode(it.fom ?: LocalDate.MIN, it.tom ?: LocalDate.MAX).overlapperMed(vurderingsperiode) }
            // TODO: Filter på utbetaler?
            .map {
                Vurdering(
                    lovreferanse = lovreferanse(),
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
                        lovreferanse = lovreferanse(),
                        kilde = SØKNADKILDE,
                        fom = null,
                        tom = null,
                        utfall = Utfall.OPPFYLT,
                        detaljer = "",
                    )
                )
            }

    override fun vurderinger(): List<Vurdering> =
        (søknadVurderinger + manuellVurdering).filterNotNull()

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
        private const val AINNTEKTKILDE = "A-Inntekt"
    }
}
