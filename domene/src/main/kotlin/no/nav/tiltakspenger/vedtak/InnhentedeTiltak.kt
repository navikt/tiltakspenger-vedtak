package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentedeTiltak(
    val tiltaksliste: List<Tiltaksaktivitet>,
    val tidsstempelInnhentet: LocalDateTime
)