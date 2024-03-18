package no.nav.tiltakspenger.vedtak.clients.utbetaling

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingReqMapper.mapUtbetalingReq

class UtbetalingGatewayImpl(private val utbetalingClient: UtbetalingClient) : UtbetalingGateway {

    override suspend fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String {
        return utbetalingClient.iverksett(mapUtbetalingReq(vedtak, sak))
    }
}
