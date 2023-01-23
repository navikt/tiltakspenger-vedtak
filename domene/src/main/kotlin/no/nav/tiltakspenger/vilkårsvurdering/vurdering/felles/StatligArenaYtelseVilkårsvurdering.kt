package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

abstract class StatligArenaYtelseVilkårsvurdering(
    val ytelser: List<YtelseSak>,
    val vurderingsperiode: Periode
) : Vilkårsvurdering() {
    val ytelseVurderinger: List<Vurdering> = lagYtelseVurderinger(ytelser, vurderingsperiode, ytelseType())

    override var manuellVurdering: Vurdering? = null

    abstract fun ytelseType(): YtelseSak.YtelseSakYtelsetype

    override fun vurderinger(): List<Vurdering> = (ytelseVurderinger + manuellVurdering).filterNotNull()

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = ytelseVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    fun lagYtelseVurderinger(
        ytelser: List<YtelseSak>,
        vurderingsperiode: Periode,
        type: YtelseSak.YtelseSakYtelsetype
    ): List<Vurdering> = ytelser
        .filter {
            Periode(
                it.fomGyldighetsperiode.toLocalDate(),
                (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)
            ).overlapperMed(vurderingsperiode)
        }
        // .filter { it.status == YtelseSak.YtelseSakStatus.AKTIV }
        .filter { it.ytelsestype == type }
        .map {
            Vurdering(
                vilkår = vilkår(),
                kilde = "Arena",
                fom = it.fomGyldighetsperiode.toLocalDate(),
                tom = it.tomGyldighetsperiode?.toLocalDate(),
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = detaljerForManuellVurdering(it),
            )
        }.ifEmpty {
            listOf(
                Vurdering(
                    vilkår = vilkår(),
                    kilde = "Arena",
                    fom = null,
                    tom = null,
                    utfall = Utfall.OPPFYLT,
                    detaljer = "",
                )
            )
        }

    private fun detaljerForManuellVurdering(sak: YtelseSak): String =
        if (sak.ytelsestype == YtelseSak.YtelseSakYtelsetype.DAGP) {
            when {
                sak.antallUkerIgjen != null && sak.antallDagerIgjen != null -> "${sak.antallUkerIgjen} uker (${sak.antallDagerIgjen} dager) igjen"
                sak.antallUkerIgjen != null && sak.antallDagerIgjen == null -> "${sak.antallUkerIgjen} uker igjen"
                sak.antallUkerIgjen == null && sak.antallDagerIgjen != null -> "${sak.antallDagerIgjen} dager igjen"
                else -> "Ukjent antall uker igjen"
            }
        } else ""
}
