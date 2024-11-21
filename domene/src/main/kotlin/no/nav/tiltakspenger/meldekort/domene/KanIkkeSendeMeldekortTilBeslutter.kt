package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.Saksbehandlerroller
import no.nav.tiltakspenger.saksbehandling.service.sak.KunneIkkeHenteSakForSakId

sealed interface KanIkkeSendeMeldekortTilBeslutter {
    data object MeldekortperiodenKanIkkeVæreFremITid : KanIkkeSendeMeldekortTilBeslutter
    data class MåVæreSaksbehandler(val roller: Saksbehandlerroller) : KanIkkeSendeMeldekortTilBeslutter
    data class ForMangeDagerUtfylt(val maksDagerMedTiltakspengerForPeriode: Int, val antallDagerUtfylt: Int) : KanIkkeSendeMeldekortTilBeslutter
    data class KunneIkkeHenteSak(val underliggende: KunneIkkeHenteSakForSakId) : KanIkkeSendeMeldekortTilBeslutter
}
