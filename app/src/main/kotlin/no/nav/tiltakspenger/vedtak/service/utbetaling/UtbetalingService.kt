package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.saksbehandling.vedtak.Vedtak

interface UtbetalingService {
    suspend fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String
}
