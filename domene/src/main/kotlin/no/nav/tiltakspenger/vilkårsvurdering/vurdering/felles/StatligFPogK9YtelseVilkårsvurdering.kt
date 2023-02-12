package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

abstract class StatligFPogK9YtelseVilkårsvurdering(
    val ytelser: List<ForeldrepengerVedtak>,
    val vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    val ytelseVurderinger: List<Vurdering> = lagYtelseVurderinger(ytelser, vurderingsperiode, ytelseType(), kilde())

    override var manuellVurdering: Vurdering? = null

    abstract fun ytelseType(): List<ForeldrepengerVedtak.Ytelser>

    abstract fun kilde(): String

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
        ytelser: List<ForeldrepengerVedtak>,
        vurderingsperiode: Periode,
        type: List<ForeldrepengerVedtak.Ytelser>,
        kilde: String,
    ): List<Vurdering> = ytelser
        .filter {
            Periode(
                it.periode.fra,
                (it.periode.til),
            ).overlapperMed(vurderingsperiode)
        }
        .filter { it.ytelse in type }
        .map {
            Vurdering(
                vilkår = vilkår(),
                kilde = it.kildesystem.name,
                fom = it.periode.fra,
                tom = it.periode.til,
                utfall = Utfall.KREVER_MANUELL_VURDERING,
                detaljer = it.tilleggsopplysninger ?: "",
            )
        }.ifEmpty {
            listOf(
                Vurdering(
                    vilkår = vilkår(),
                    kilde = kilde,
                    fom = null,
                    tom = null,
                    utfall = Utfall.OPPFYLT,
                    detaljer = "",
                ),
            )
        }
}
