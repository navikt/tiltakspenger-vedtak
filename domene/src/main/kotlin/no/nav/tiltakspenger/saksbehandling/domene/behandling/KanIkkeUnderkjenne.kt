package no.nav.tiltakspenger.saksbehandling.domene.behandling

sealed interface KanIkkeUnderkjenne {
    data object MåVæreBeslutter : KanIkkeUnderkjenne
}
