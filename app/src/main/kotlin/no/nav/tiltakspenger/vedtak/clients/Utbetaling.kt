package no.nav.tiltakspenger.vedtak.clients

import no.nav.tiltakspenger.domene.behandling.Behandling

interface Utbetaling {
    suspend fun iverksett(behandling: Behandling): String
}
