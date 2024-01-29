package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface UtbetalingService {
    suspend fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String
}
