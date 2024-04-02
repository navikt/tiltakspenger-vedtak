package no.nav.tiltakspenger.innsending.domene

import java.time.LocalDateTime

data class InnhentedeArenaYtelser(
    val ytelserliste: List<YtelseSak>,
    val tidsstempelInnhentet: LocalDateTime,
)
