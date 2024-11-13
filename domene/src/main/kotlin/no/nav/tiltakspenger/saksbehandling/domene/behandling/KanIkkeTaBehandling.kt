package no.nav.tiltakspenger.saksbehandling.domene.behandling

sealed interface KanIkkeTaBehandling {
    data object MåVæreSaksbehandlerEllerBeslutter : KanIkkeTaBehandling
}
