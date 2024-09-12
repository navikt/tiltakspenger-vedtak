package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.Roller

sealed interface KanIkkeIverksetteMeldekort {
    data class MåVæreBeslutter(val roller: Roller) : KanIkkeIverksetteMeldekort
    data object SaksbehandlerOgBeslutterKanIkkeVæreLik : KanIkkeIverksetteMeldekort
}
