package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import java.time.LocalDate

sealed class StatligYtelseVilkårsvurdering : Vilkårsvurdering() {
    abstract val ytelseVurderinger: List<Vurdering>
    abstract override var manuellVurdering: Vurdering?

    override fun vurderinger(): List<Vurdering> = (ytelseVurderinger + manuellVurdering).filterNotNull()

    override fun samletUtfallYtelser(): Utfall {
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
    ): List<Vurdering> =
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

    data class AAPVilkårsvurdering(
        private val ytelser: List<YtelseSak>,
        private val vurderingsperiode: Periode,
    ) : StatligYtelseVilkårsvurdering() {
        override val lovReferanse: Lovreferanse = Lovreferanse.AAP
        override var manuellVurdering: Vurdering? = null

        override val ytelseVurderinger: List<Vurdering> =
            lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.AA)
    }

    data class DagpengerVilkårsvurdering(
        private val ytelser: List<YtelseSak>,
        private val vurderingsperiode: Periode,
    ) : StatligYtelseVilkårsvurdering() {
        override val lovReferanse: Lovreferanse = Lovreferanse.DAGPENGER
        override var manuellVurdering: Vurdering? = null

        override val ytelseVurderinger: List<Vurdering> =
            lagYtelseVurderinger(ytelser, vurderingsperiode, YtelseSak.YtelseSakYtelsetype.DAGP)
    }
}
