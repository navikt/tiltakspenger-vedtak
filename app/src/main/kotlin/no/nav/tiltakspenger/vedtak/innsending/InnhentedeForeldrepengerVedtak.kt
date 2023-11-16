package no.nav.tiltakspenger.vedtak.innsending

import java.time.LocalDateTime

data class InnhentedeForeldrepengerVedtak(
    val foreldrepengerVedtakliste: List<ForeldrepengerVedtak>,
    val tidsstempelInnhentet: LocalDateTime,
)
