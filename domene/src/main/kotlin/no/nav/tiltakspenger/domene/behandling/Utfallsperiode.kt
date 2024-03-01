package no.nav.tiltakspenger.domene.behandling

import java.time.LocalDate

data class Utfallsperiode(
    val fom: LocalDate,
    val tom: LocalDate,
    val antallBarn: Int,
    val tiltak: List<Tiltak>,
    val antDagerMedTiltak: Int,
    val utfall: UtfallForPeriode,
) {
    override fun equals(other: Any?): Boolean {
        return other != null &&
            other is Utfallsperiode &&
            this.antallBarn == other.antallBarn &&
            this.utfall == other.utfall &&
            this.antDagerMedTiltak == other.antDagerMedTiltak
        // husk tiltak
    }

    override fun hashCode(): Int {
        var result = fom.hashCode()
        result = 31 * result + tom.hashCode()
        result = 31 * result + antallBarn
        result = 31 * result + tiltak.hashCode()
        result = 31 * result + antDagerMedTiltak
        result = 31 * result + utfall.hashCode()
        return result
    }
}

enum class UtfallForPeriode {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
    KREVER_MANUELL_VURDERING,
}
