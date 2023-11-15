package no.nav.tiltakspenger.vedtak.innsending

import java.time.LocalDateTime

data class InnhentetUføre(
    val uføreVedtak: UføreVedtak?,
    val tidsstempelInnhentet: LocalDateTime,
)
