package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

data class InnhentedeArenaYtelser(
    val ytelserliste: List<YtelseSak>,
    val tidsstempelInnhentet: LocalDateTime,
)
