package no.nav.tiltakspenger.innsending

import java.time.LocalDateTime

data class InnhentedeOvergangsstønadVedtak(
    val overgangsstønadVedtak: List<OvergangsstønadVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
