package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentedeOvergangsstønadVedtak(
    val overgangsstønadVedtak: List<OvergangsstønadVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
