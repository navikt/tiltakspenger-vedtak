package no.nav.tiltakspenger.innsending

import java.time.LocalDateTime

data class InnhentetUføre(
    val uføreVedtak: UføreVedtak?,
    val tidsstempelInnhentet: LocalDateTime,
)
