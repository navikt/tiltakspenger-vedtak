package no.nav.tiltakspenger.innsending.domene

import java.time.LocalDateTime

data class InnhentetUføre(
    val uføreVedtak: UføreVedtak?,
    val tidsstempelInnhentet: LocalDateTime,
)
