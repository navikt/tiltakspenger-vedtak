package no.nav.tiltakspenger.saksbehandling.domene.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDateTime

data class Tiltak(
    val id: TiltakId,
    val eksternId: String,
    val gjennomføring: Gjennomføring,
    val deltakelsesperiode: Periode,
    val deltakelseStatus: TiltakDeltakerstatus,
    val deltakelseProsent: Float?,
    val antallDagerPerUke: Float?,
    val kilde: Tiltakskilde,
    val registrertDato: LocalDateTime,
    val innhentetTidspunkt: LocalDateTime,
) {
    data class Gjennomføring(
        val id: String,
        // TODO ukjent: kan vi slette denne?
        val arrangørnavn: String,
        val typeNavn: String,
        val typeKode: String,
        val rettPåTiltakspenger: Boolean,
    )
}
