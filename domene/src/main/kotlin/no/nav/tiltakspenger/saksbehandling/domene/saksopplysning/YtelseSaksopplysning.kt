package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.dekkerHele
import no.nav.tiltakspenger.felles.erInnenfor
import no.nav.tiltakspenger.felles.inneholderOverlapp
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class YtelseSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val subperioder: List<HarYtelsePeriode>,
) : SaksopplysningInterface {
    companion object {
        val VILKÅR_KREVER_MANUELL = listOf(
            Vilkår.AAP,
            Vilkår.DAGPENGER,
            Vilkår.TILTAKSPENGER,
        )
    }

    fun erTom(): Boolean {
        return subperioder.isEmpty()
    }

    fun inneholderOverlapp(): Boolean {
        return subperioder.map { it.periode }.inneholderOverlapp()
    }

    fun dekkerHele(vurderingsperiode: Periode): Boolean { // Misvisende metodenavn
        return subperioder.map { it.periode }.dekkerHele(vurderingsperiode)
    }

    fun erInnenfor(vurderingsperiode: Periode): Boolean {
        return subperioder.map { it.periode }.erInnenfor(vurderingsperiode)
    }

    fun vilkårsvurder(): List<Vurdering> =
        subperioder.map { harYtelsePeriode ->
            Vurdering(
                vilkår = vilkår,
                kilde = kilde,
                fom = harYtelsePeriode.periode.fra,
                tom = harYtelsePeriode.periode.til,
                utfall = if (harYtelsePeriode.harYtelse) {
                    if (vilkår in VILKÅR_KREVER_MANUELL) {
                        Utfall.KREVER_MANUELL_VURDERING
                    } else {
                        Utfall.IKKE_OPPFYLT
                    }
                } else {
                    Utfall.OPPFYLT
                },
                detaljer = detaljer,
            )
        }
}

data class HarYtelsePeriode(
    val periode: Periode,
    val harYtelse: Boolean,
)
