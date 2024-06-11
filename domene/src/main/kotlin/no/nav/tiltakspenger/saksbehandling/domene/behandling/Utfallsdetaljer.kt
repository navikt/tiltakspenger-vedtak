package no.nav.tiltakspenger.saksbehandling.domene.behandling

data class Utfallsdetaljer(
    val antallBarn: Int,
    val utfall: UtfallForPeriode,
)

enum class UtfallForPeriode {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
    KREVER_MANUELL_VURDERING,
}
