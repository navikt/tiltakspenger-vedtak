package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentedeForeldrepengerVedtak(
    val foreldrepengerVedtakliste: List<ForeldrepengerVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
