package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortStatus

enum class MeldekortstatusDTO {
    IKKE_KLAR_TIL_UTFYLLING,
    KLAR_TIL_UTFYLLING,
    KLAR_TIL_BESLUTNING,
    GODKJENT,
}

fun Meldekort.toMeldekortstatusDTO(): MeldekortstatusDTO {
    return when (val m = this) {
        is Meldekort.IkkeUtfyltMeldekort -> if (m.erKlarTilUtfylling()) MeldekortstatusDTO.KLAR_TIL_UTFYLLING else MeldekortstatusDTO.IKKE_KLAR_TIL_UTFYLLING
        is Meldekort.UtfyltMeldekort -> when (this.status) {
            MeldekortStatus.KLAR_TIL_BESLUTNING -> MeldekortstatusDTO.KLAR_TIL_BESLUTNING
            MeldekortStatus.GODKJENT -> MeldekortstatusDTO.GODKJENT
            MeldekortStatus.IKKE_UTFYLT -> throw IllegalStateException("Utfylt meldekort kan ikke ha status IKKE_UTFYLT")
        }
    }
}
