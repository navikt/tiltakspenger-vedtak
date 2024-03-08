package no.nav.tiltakspenger.innsending

import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Kilde
import java.time.LocalDate
import java.time.LocalDateTime

data class OvergangsstønadVedtak(
    val id: OvergangsstønadVedtakId,
    val fom: LocalDate,
    val tom: LocalDate,
    val datakilde: Kilde,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
