package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

interface IDelvisAutomatiskVilkårsvurdering {
    fun vurderinger(): List<Vurdering>

    fun detIkkeManuelleUtfallet(): Utfall
}


interface IDelvisManuellVilkårsvurdering {

    fun manuellVurdering(): Vurdering?

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String)
}

class DelvisManuellVilkårsvurderingKomponent : IDelvisManuellVilkårsvurdering {
    private var manuellVurdering: Vurdering? = null
    override fun manuellVurdering(): Vurdering? = manuellVurdering

    override fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        manuellVurdering = Vurdering(
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}
