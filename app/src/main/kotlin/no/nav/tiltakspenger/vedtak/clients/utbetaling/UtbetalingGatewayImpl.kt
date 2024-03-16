package no.nav.tiltakspenger.vedtak.clients.utbetaling

import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.domene.sak.SakDetaljer
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingReqMapper.mapUtbetalingReq
import no.nav.tiltakspenger.vedtak.service.ports.UtbetalingGateway

class UtbetalingGatewayImpl(private val utbetalingClient: UtbetalingClient) : UtbetalingGateway {

    override fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String {
        return runBlocking {
            utbetalingClient.iverksett(mapUtbetalingReq(vedtak, sak))
        }
    }
}
