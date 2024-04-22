package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering

data class YtelseSaksopplysning(
    override val kilde: Kilde,
    override val vilkår: Vilkår,
    override val detaljer: String,
    override val saksbehandler: String? = null,
    val periode: Periode,
    val harYtelse: Boolean,
) : SaksopplysningInterface {
    companion object {
        val VILKÅR_KREVER_MANUELL = listOf(
            Vilkår.AAP,
            Vilkår.DAGPENGER,
            Vilkår.TILTAKSPENGER,
        )
    }

    fun vilkårsvurder(vurderingsperiode: Periode): Vurdering =
        Vurdering(
            vilkår = vilkår,
            kilde = kilde,
            fom = vurderingsperiode.fra,
            tom = vurderingsperiode.til,
            utfall = if (harYtelse) {
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

