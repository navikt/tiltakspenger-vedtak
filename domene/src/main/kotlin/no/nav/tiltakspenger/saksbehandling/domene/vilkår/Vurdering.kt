package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate

sealed class Vurdering {
    abstract val vilkår: Vilkår
    abstract val kilde: Kilde
    abstract val fom: LocalDate?
    abstract val tom: LocalDate?
    abstract val utfall: Utfall
    abstract val detaljer: String

    data class Oppfylt(
        override val vilkår: Vilkår,
        override val kilde: Kilde,
        override val detaljer: String,
        override val fom: LocalDate,
        override val tom: LocalDate,
    ) : Vurdering() {
        override val utfall = Utfall.OPPFYLT
    }

    data class IkkeOppfylt(
        override val vilkår: Vilkår,
        override val kilde: Kilde,
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val detaljer: String,
    ) : Vurdering() {
        override val utfall = Utfall.IKKE_OPPFYLT
    }

    data class KreverManuellVurdering(
        override val vilkår: Vilkår,
        override val kilde: Kilde,
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val detaljer: String,
    ) : Vurdering() {
        override val utfall = Utfall.KREVER_MANUELL_VURDERING
    }
}
