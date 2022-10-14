package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import java.time.LocalDate

class AAPVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
) {
    val lovReferanse: Lovreferanse = Lovreferanse.AAP
    private val ytelseVurderinger = lagYtelseVurderinger()

    private fun lagYtelseVurderinger(): List<Vurdering> =
        ytelser
            .filter {
                Periode(
                    it.fomGyldighetsperiode.toLocalDate(),
                    (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)
                ).overlapperMed(vurderingsperiode)
            }.filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.AA }
            .map {
                Vurdering(
                    kilde = "Arena",
                    fom = it.fomGyldighetsperiode.toLocalDate(),
                    tom = it.tomGyldighetsperiode?.toLocalDate(),
                    utfall = Utfall.IKKE_OPPFYLT,
                    detaljer = "",
                )
            }.ifEmpty {
                listOf(
                    Vurdering(
                        kilde = "Arena",
                        fom = null,
                        tom = null,
                        utfall = Utfall.OPPFYLT,
                        detaljer = "",
                    )
                )
            }

    fun vurderinger(): List<Vurdering> = ytelseVurderinger
}
