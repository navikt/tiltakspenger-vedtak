package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

sealed interface Revurderingsbehandling : Behandling {
    val forrigeVedtak: Vedtak
}
