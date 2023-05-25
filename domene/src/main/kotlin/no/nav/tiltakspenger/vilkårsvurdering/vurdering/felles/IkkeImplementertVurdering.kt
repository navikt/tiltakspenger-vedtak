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
                Vurdering.IkkeImplementert(
                    vilkår = vilkår(),
                    kilde = kilde(),
                    fom = vurderingsperiode.fra,
                    tom = vurderingsperiode.til,
                    detaljer = "",
                ),
            )
        }

    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.IKKE_IMPLEMENTERT

    abstract fun kilde(): String
}
