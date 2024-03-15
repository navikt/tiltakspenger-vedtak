package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.service.ports.SakRepo
import no.nav.tiltakspenger.vedtak.service.ports.UtbetalingGateway

class UtbetalingServiceImpl(
    private val utbetalingGateway: UtbetalingGateway,
    private val sakRepo: SakRepo,
) : UtbetalingService {
    override fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String {
        val sak = sakRepo.hentSakDetaljer(vedtak.sakId) ?: throw IllegalStateException("Fant ikke sak for vedtak")
        return utbetalingGateway.iverksett(vedtak, sak)
    }
}
