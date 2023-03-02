package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

data class OvergangsstønadVilkårsvurdering(
    private val overgangsstønadVedtak: List<OvergangsstønadVedtak>,
    private val vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    val overgangsstønadVurderinger: List<Vurdering> = lagVurderingerFraVedtak()
    override fun vilkår(): Vilkår = Vilkår.OVERGANGSSTØNAD

    override var manuellVurdering: Vurdering? = null

    override fun vurderinger(): List<Vurdering> = (overgangsstønadVurderinger + manuellVurdering).filterNotNull()
    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = overgangsstønadVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    private fun lagVurderingerFraVedtak(): List<Vurdering> = overgangsstønadVedtak
        .filter {
            Periode(
                it.fom,
                (it.tom),
            ).overlapperMed(vurderingsperiode)
        }.map {
            Vurdering(
                vilkår = vilkår(),
                kilde = it.datakilde,
                fom = it.fom,
                tom = it.tom,
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = "",
            )
        }.ifEmpty {
            listOf(
                Vurdering(
                    vilkår = vilkår(),
                    kilde = KILDE,
                    fom = null,
                    tom = null,
                    utfall = Utfall.OPPFYLT,
                    detaljer = "",
                ),
            )
        }

    companion object {
        private const val KILDE = "EF"
    }
}
