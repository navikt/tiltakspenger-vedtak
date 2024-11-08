package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.saksbehandling.service.sak.KunneIkkeHenteSakForSakId

sealed interface KanIkkeIverksetteMeldekort {
    data class MåVæreBeslutter(val roller: Roller) : KanIkkeIverksetteMeldekort
    data object SaksbehandlerOgBeslutterKanIkkeVæreLik : KanIkkeIverksetteMeldekort
    data class KunneIkkeHenteSak(val underliggende: KunneIkkeHenteSakForSakId) : KanIkkeIverksetteMeldekort
}
