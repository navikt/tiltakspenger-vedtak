package no.nav.tiltakspenger.vedtak.clients.utbetaling

import no.nav.tiltakspenger.domene.sak.SakDetaljer
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingReqMapper.mapUtbetalingReq
import no.nav.tiltakspenger.vedtak.service.ports.UtbetalingGateway

class UtbetalingGatewayImpl(private val utbetalingClient: UtbetalingClient) : UtbetalingGateway {

    override suspend fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String {
        return utbetalingClient.iverksett(mapUtbetalingReq(vedtak, sak))
    }
}
