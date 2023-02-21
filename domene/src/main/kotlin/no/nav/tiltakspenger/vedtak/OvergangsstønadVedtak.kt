package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import java.time.LocalDate
import java.time.LocalDateTime

data class OvergangsstønadVedtak(
    val id: OvergangsstønadVedtakId,
    val fom: LocalDate,
    val tom: LocalDate,
    val datakilde: String,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
