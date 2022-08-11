package no.nav.tiltakspenger.vedtak

import java.time.LocalDate

data class Tiltaksaktivitet(
    val tiltaksnavn: String,
    val aktivitetId: String,
    val tiltakLokaltNavn: String?,
    val arrangoer: String?,
    val bedriftsnummer: String?,
    val deltakelsePeriode: DeltakelsesPeriode?,
    val deltakelseProsent: Float?,
    val deltakerStatus: DeltakerStatus,
    val statusSistEndret: LocalDate?,
    val begrunnelseInnsoeking: String,
    val antallDagerPerUke: Float?,
) {

    data class DeltakelsesPeriode(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatus(
        val termnavn: String,
        val status: String
    )
}