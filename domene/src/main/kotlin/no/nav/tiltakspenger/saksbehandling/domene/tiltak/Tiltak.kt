package no.nav.tiltakspenger.saksbehandling.domene.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import java.time.LocalDate
import java.time.LocalDateTime

data class Tiltak(
    val id: TiltakId,
    val eksternId: String,
    val gjennomføring: Gjennomføring,
    val deltakelseFom: LocalDate,
    val deltakelseTom: LocalDate,
    val deltakelseStatus: String,
    val deltakelseProsent: Float?,
    val kilde: Tiltakskilde,
    val registrertDato: LocalDateTime,
    val innhentet: LocalDateTime,
) {
    data class Gjennomføring(
        val id: String,
        val arrangørnavn: String, // kan vi slette denne?
        val typeNavn: String,
        val typeKode: String,
        val rettPåTiltakspenger: Boolean,
    )
}
