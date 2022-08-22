package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

data class TiltaksaktivitetDTO(
    val tiltaksnavn: String,
    val aktivitetId: String,
    val tiltakLokaltNavn: String?,
    val arrangoer: String?,
    val bedriftsnummer: String?,
    val deltakelsePeriode: DeltakelsesPeriodeDTO?,
//    @JsonDeserialize(using = ArenaFloatDeserializer::class)
    val deltakelseProsent: Float?,
    val deltakerStatus: DeltakerStatusDTO,
    val statusSistEndret: LocalDate?,
    val begrunnelseInnsoeking: String,
//    @JsonDeserialize(using = ArenaFloatDeserializer::class)
    val antallDagerPerUke: Float?,
) {

    data class DeltakelsesPeriodeDTO(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatusDTO(
        val termnavn: String,
        val innerText: String
    )
}