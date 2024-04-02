package no.nav.tiltakspenger.innsending.domene

import java.time.LocalDateTime

data class InnhentedeOvergangsstønadVedtak(
    val overgangsstønadVedtak: List<OvergangsstønadVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
