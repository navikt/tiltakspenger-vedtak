package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.vedtak.Vedtak

sealed interface Revurderingsbehandling : Behandling {
    val forrigeVedtak: Vedtak
}
