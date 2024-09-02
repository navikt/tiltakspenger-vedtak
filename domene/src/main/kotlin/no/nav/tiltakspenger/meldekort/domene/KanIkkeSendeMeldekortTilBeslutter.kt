package no.nav.tiltakspenger.meldekort.domene

sealed interface KanIkkeSendeMeldekortTilBeslutter {
    data object MeldekortperiodenKanIkkeVæreFremITid : KanIkkeSendeMeldekortTilBeslutter
}
