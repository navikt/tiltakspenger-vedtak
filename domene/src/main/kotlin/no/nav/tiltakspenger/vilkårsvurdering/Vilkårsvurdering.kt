package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

sealed class Vilkårsvurdering {
    abstract val lovReferanse: Lovreferanse
    abstract var manuellVurdering: Vurdering?

    abstract fun vurderinger(): List<Vurdering>
    abstract fun samletUtfallYtelser(): Utfall

    fun samletUtfall() = manuellVurdering?.utfall ?: samletUtfallYtelser()

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
