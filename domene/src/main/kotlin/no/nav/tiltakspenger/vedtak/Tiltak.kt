package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

data class Tiltak(
    val id: String,
    val gjennomføring: Gjennomføring,
    val deltakelseFom: LocalDate?,
    val deltakelseTom: LocalDate?,
    val deltakelseStatus: DeltakerStatus,
    val deltakelseDagerUke: Float?,
    val deltakelseProsent: Float?,
    val kilde: String,
    val registrertDato: LocalDateTime,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde(): LocalDateTime = registrertDato
    override fun tidsstempelHosOss(): LocalDateTime = innhentet

    data class Gjennomføring(
        val id: String,
        val arrangørnavn: String,
        val typeNavn: String,
        val typeKode: String,
        val rettPåTiltakspenger: Boolean,
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatus(
        val status: String,
        val rettTilÅASøke: Boolean,
    )
}
