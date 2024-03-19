package no.nav.tiltakspenger.saksbehandling.domene.behandling

import java.time.LocalDate

data class Utfallsperiode(
    val fom: LocalDate,
    val tom: LocalDate,
    val antallBarn: Int,
    val utfall: UtfallForPeriode,
) {
    fun kanSlåsSammen(other: Utfallsperiode): Boolean {
        return this.antallBarn == other.antallBarn && this.utfall == other.utfall
    }
}

enum class UtfallForPeriode {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
    KREVER_MANUELL_VURDERING,
}
