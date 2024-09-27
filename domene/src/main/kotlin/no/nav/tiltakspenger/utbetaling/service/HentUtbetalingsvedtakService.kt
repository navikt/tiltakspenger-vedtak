package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

class HentUtbetalingsvedtakService(
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
) {
    fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak? = utbetalingsvedtakRepo.hentForVedtakId(vedtakId)
}
