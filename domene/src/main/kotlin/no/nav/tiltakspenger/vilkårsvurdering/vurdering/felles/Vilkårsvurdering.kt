package no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.VurderingType
import java.time.LocalDate

abstract class Vilkårsvurdering {
    protected abstract var vurderinger: List<Vurdering>
    abstract fun vilkår(): Vilkår

    fun vurderinger() = vurderinger

    fun List<Vurdering>.utfall(): Utfall {
        val utfall = this.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            utfall.all { it == Utfall.IKKE_IMPLEMENTERT } -> Utfall.IKKE_IMPLEMENTERT
            else -> Utfall.OPPFYLT
        }
    }

    fun samletUtfall() = vurderinger.filter { it.vurderingType == VurderingType.MANUELL }.firstOrNull()?.utfall
        ?: vurderinger.utfall()

    fun settManuellVurdering(
        fom: LocalDate,
        tom: LocalDate,
        utfall: Utfall,
        detaljer: String
    ) {
        vurderinger += Vurdering(
            vilkår = vilkår(),
            vurderingType = VurderingType.MANUELL,
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}
