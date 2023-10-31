package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.behandling.Behandling

interface UtbetalingService {
    fun sendBehandlingTilUtbetaling(behandling: Behandling)
}
