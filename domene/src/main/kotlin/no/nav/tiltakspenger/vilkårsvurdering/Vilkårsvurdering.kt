package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

sealed class Vilkårsvurdering {
    abstract val lovreferanse: Lovreferanse
    abstract var manuellVurdering: Vurdering?

    abstract fun vurderinger(): List<Vurdering>
    abstract fun detIkkeManuelleUtfallet(): Utfall

    fun samletUtfall() = manuellVurdering?.utfall ?: detIkkeManuelleUtfallet()

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        manuellVurdering = Vurdering(
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}
