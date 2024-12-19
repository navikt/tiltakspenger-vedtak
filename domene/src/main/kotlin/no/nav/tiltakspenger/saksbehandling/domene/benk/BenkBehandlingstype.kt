package no.nav.tiltakspenger.saksbehandling.domene.benk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingstype

enum class BenkBehandlingstype {
    SØKNAD,
    FØRSTEGANGSBEHANDLING,
    REVURDERING,
}

fun Behandlingstype.toBenkBehandlingstype(): BenkBehandlingstype =
    when (this) {
        Behandlingstype.FØRSTEGANGSBEHANDLING -> BenkBehandlingstype.FØRSTEGANGSBEHANDLING
        Behandlingstype.REVURDERING -> BenkBehandlingstype.REVURDERING
        else -> BenkBehandlingstype.SØKNAD
    }