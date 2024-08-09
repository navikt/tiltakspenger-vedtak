package no.nav.tiltakspenger.saksbehandling.domene.behandling

sealed interface KanIkkeOppretteBehandling {
    data object StøtterIkkeBarnetillegg : KanIkkeOppretteBehandling

    data object FantIkkeTiltak : KanIkkeOppretteBehandling
}
