package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import java.time.LocalDate

class BaseStatligYtelseVilkårsvurdering(
    private val ytelser: List<YtelseSak>,
    private val vurderingsperiode: Periode,
    private val type: YtelseSak.YtelseSakYtelsetype,
) : IAutomatiskVilkårsvurdering {

    override fun vurderinger(): List<Vurdering> =
        lagYtelseVurderinger()

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = lagYtelseVurderinger().map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    private fun lagYtelseVurderinger(): List<Vurdering> =
        ytelser
            .filter {
                Periode(
                    it.fomGyldighetsperiode.toLocalDate(),
                    (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)
                ).overlapperMed(vurderingsperiode)
            }
            .filter { it.status == YtelseSak.YtelseSakStatus.AKTIV }
            .filter { it.ytelsestype == type }
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
}
