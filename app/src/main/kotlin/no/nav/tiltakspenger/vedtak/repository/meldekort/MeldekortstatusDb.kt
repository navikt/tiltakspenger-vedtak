package no.nav.tiltakspenger.vedtak.repository.meldekort

import no.nav.tiltakspenger.meldekort.domene.MeldekortStatus

/**
 * @see MeldekortStatus
 */

private enum class MeldekortstatusDb {
    KLAR_TIL_UTFYLLING,
    KLAR_TIL_BESLUTNING,
    GODKJENT,
}

fun String.toMeldekortStatus(): MeldekortStatus =
    when (MeldekortstatusDb.valueOf(this)) {
        MeldekortstatusDb.KLAR_TIL_UTFYLLING -> MeldekortStatus.KLAR_TIL_UTFYLLING
        MeldekortstatusDb.KLAR_TIL_BESLUTNING -> MeldekortStatus.KLAR_TIL_BESLUTNING
        MeldekortstatusDb.GODKJENT -> MeldekortStatus.GODKJENT
    }

fun MeldekortStatus.toDb(): String =
    when (this) {
        MeldekortStatus.KLAR_TIL_UTFYLLING -> MeldekortstatusDb.KLAR_TIL_UTFYLLING
        MeldekortStatus.KLAR_TIL_BESLUTNING -> MeldekortstatusDb.KLAR_TIL_BESLUTNING
        MeldekortStatus.GODKJENT -> MeldekortstatusDb.GODKJENT
    }.toString()
