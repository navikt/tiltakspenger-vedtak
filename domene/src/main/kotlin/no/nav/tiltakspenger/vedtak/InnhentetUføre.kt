package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentetUføre(
    val uføreVedtak: UføreVedtak?,
    val tidsstempelInnhentet: LocalDateTime,
)
