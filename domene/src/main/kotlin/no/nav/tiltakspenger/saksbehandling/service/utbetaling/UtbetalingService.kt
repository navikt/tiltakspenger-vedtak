package no.nav.tiltakspenger.saksbehandling.service.utbetaling

import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface UtbetalingService {
    suspend fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String
}
