package no.nav.tiltakspenger.innsending

import java.time.LocalDateTime

data class InnhentedeForeldrepengerVedtak(
    val foreldrepengerVedtakliste: List<ForeldrepengerVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
