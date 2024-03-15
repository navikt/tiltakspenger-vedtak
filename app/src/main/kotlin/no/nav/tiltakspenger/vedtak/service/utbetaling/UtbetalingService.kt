package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface UtbetalingService {
    fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String
}
