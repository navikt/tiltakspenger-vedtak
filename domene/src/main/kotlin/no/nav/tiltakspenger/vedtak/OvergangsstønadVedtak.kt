package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import java.time.LocalDateTime

data class OvergangsstønadVedtak(
    val id: OvergangsstønadVedtakId,
    val periode: OvergangsstønadPeriode,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde() = innhentet

    override fun tidsstempelHosOss() = innhentet
}
