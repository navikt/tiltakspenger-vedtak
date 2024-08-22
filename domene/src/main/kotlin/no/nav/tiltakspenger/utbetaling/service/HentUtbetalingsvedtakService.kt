package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

class HentUtbetalingsvedtakService(
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
) {
    fun hentForBehandlingId(behandlingId: BehandlingId): List<Utbetalingsvedtak> {
        return utbetalingsvedtakRepo.hentForFørstegangsbehandlingId(behandlingId)
    }

    fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak? = utbetalingsvedtakRepo.hentForVedtakId(vedtakId)
}
