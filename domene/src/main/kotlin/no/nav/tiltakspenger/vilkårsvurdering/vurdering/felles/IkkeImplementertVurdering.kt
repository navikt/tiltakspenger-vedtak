package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

abstract class IkkeImplementertVurdering : Vilkårsvurdering() {
    override var manuellVurdering: Vurdering? = null
    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering)
        .ifEmpty {
            listOf(
                Vurdering(
                    lovreferanse = lovreferanse(),
                    kilde = kilde(),
                    fom = null,
                    tom = null,
                    utfall = Utfall.IKKE_IMPLEMENTERT,
                    detaljer = ""
                )
            )
        }

    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.IKKE_IMPLEMENTERT

    abstract fun kilde(): String
}
