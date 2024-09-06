package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.meldekort.domene.MeldekortStatus

enum class MeldekortstatusDTO {
    KLAR_TIL_UTFYLLING,
    KLAR_TIL_BESLUTNING,
    GODKJENT,
}
fun MeldekortStatus.toDTO(): MeldekortstatusDTO =
    when (this) {
        MeldekortStatus.GODKJENT -> MeldekortstatusDTO.GODKJENT
        MeldekortStatus.KLAR_TIL_UTFYLLING -> MeldekortstatusDTO.KLAR_TIL_UTFYLLING
        MeldekortStatus.KLAR_TIL_BESLUTNING -> MeldekortstatusDTO.KLAR_TIL_BESLUTNING
    }
