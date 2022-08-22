package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

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
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun oppdatert(): LocalDateTime = statusSistEndret?.atStartOfDay() ?: innhentet
    override fun innhentet(): LocalDateTime = innhentet

    data class DeltakelsesPeriode(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatus(
        val termnavn: String,
        val status: String
    )
}