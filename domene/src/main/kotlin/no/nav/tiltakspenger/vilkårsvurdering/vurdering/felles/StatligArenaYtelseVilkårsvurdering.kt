package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

abstract class StatligArenaYtelseVilkårsvurdering : Vilkårsvurdering() {
    abstract val ytelseVurderinger: List<Vurdering>
    abstract override var manuellVurdering: Vurdering?

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
        //.filter { it.status == YtelseSak.YtelseSakStatus.AKTIV }
        .filter { it.ytelsestype == type }
        .map {
            Vurdering(
                vilkår = vilkår(),
                kilde = "Arena",
                fom = it.fomGyldighetsperiode.toLocalDate(),
                tom = it.tomGyldighetsperiode?.toLocalDate(),
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = "",
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
}
