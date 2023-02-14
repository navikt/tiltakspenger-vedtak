package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

abstract class Vilkårsvurdering {
    abstract fun vilkår(): Vilkår
    abstract var manuellVurdering: Vurdering?

    abstract fun vurderinger(): List<Vurdering>
    abstract fun detIkkeManuelleUtfallet(): Utfall

    fun samletUtfall() = manuellVurdering?.utfall ?: detIkkeManuelleUtfallet()

    fun settManuellVurdering(
        fom: LocalDate,
        tom: LocalDate,
        utfall: Utfall,
        detaljer: String,
    ) {
        manuellVurdering = Vurdering(
            vilkår = vilkår(),
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer,
        )
    }
}
