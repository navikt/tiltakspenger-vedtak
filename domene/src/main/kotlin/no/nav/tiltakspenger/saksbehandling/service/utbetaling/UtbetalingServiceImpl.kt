package no.nav.tiltakspenger.saksbehandling.service.utbetaling

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway

class UtbetalingServiceImpl(
    private val utbetalingGateway: UtbetalingGateway,
) : UtbetalingService {
    override suspend fun sendBehandlingTilUtbetaling(sak: SakDetaljer, vedtak: Vedtak): String {
        return utbetalingGateway.iverksett(vedtak, sak)
    }
}
