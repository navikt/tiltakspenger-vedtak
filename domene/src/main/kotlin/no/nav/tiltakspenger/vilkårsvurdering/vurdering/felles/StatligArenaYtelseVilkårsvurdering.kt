package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import java.time.LocalDate

abstract class StatligArenaYtelseVilkårsvurdering : Vilkårsvurdering() {
    abstract override var vurderinger: List<Vurdering>

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
                vurderingType = VurderingType.AUTOMATISK,
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
                    vurderingType = VurderingType.AUTOMATISK,
                    kilde = "Arena",
                    fom = null,
                    tom = null,
                    utfall = Utfall.OPPFYLT,
                    detaljer = "",
                )
            )
        }

    fun ikkeImplementertVurdering(kilde: String) =
        Vurdering(
            vilkår = vilkår(),
            vurderingType = VurderingType.AUTOMATISK,
            kilde = kilde,
            fom = null,
            tom = null,
            utfall = Utfall.IKKE_IMPLEMENTERT,
            detaljer = ""
        )
}
