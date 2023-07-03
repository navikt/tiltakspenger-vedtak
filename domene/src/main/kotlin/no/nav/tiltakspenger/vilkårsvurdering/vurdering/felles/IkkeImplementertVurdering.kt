package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

abstract class IkkeImplementertVurdering(
    private val vurderingsperiode: Periode,
) : Vilkårsvurdering() {
    override var manuellVurdering: Vurdering? = null
    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering)
        .ifEmpty {
            listOf(
                Vurdering.KreverManuellVurdering(
                    vilkår = vilkår(),
                    kilde = kilde(),
                    fom = vurderingsperiode.fra,
                    tom = vurderingsperiode.til,
                    detaljer = "Ikke implementert",
                ),
            )
        }

    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.KREVER_MANUELL_VURDERING

    abstract fun kilde(): String
}
