package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentedePersonopplysninger(
    val personopplysningerliste: List<Personopplysninger>,
    val tidsstempelInnhentet: LocalDateTime
)
