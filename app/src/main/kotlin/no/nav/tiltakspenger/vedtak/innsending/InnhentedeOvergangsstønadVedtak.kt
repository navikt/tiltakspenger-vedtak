package no.nav.tiltakspenger.vedtak.innsending

import java.time.LocalDateTime

data class InnhentedeOvergangsstønadVedtak(
    val overgangsstønadVedtak: List<OvergangsstønadVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
