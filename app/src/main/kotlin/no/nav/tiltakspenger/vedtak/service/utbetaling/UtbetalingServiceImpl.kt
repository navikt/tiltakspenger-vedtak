package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.vedtak.clients.UtbetalingClient

class UtbetalingServiceImpl(val utbetalingClient: UtbetalingClient) : UtbetalingService {
    override suspend fun sendBehandlingTilUtbetaling(behandling: Behandling): String {
        return utbetalingClient.iverksett(behandling)
    }
}
