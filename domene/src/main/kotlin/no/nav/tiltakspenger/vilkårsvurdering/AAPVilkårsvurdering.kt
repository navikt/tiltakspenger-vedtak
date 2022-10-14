package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak

class AAPVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
) {
    val lovReferanse: Lovreferanse = Lovreferanse.AAP
    private val ytelseVurdering = lagYtelseVurdering()

    private fun lagYtelseVurdering() = Vurdering(
        kilde = "Arena",
        fom = ytelser.first().fomGyldighetsperiode.toLocalDate(),
        tom = ytelser.first().fomGyldighetsperiode.toLocalDate(),
        utfall = Utfall.KREVER_MANUELL_VURDERING,
        detaljer = "",
    )

    fun vurderinger(): List<Vurdering> = listOfNotNull(ytelseVurdering)
}
