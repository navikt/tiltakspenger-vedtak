package no.nav.tiltakspenger.saksbehandling.service.utbetaling

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface UtbetalingService {
    suspend fun sendBehandlingTilUtbetaling(sak: SakDetaljer, vedtak: Vedtak): String
}
