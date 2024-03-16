package no.nav.tiltakspenger.vedtak.clients.utbetaling

import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.service.ports.UtbetalingGateway
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingReqMapper.mapUtbetalingReq

class UtbetalingGatewayImpl(private val utbetalingClient: UtbetalingClient) : UtbetalingGateway {

    override fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String {
        return runBlocking {
            utbetalingClient.iverksett(mapUtbetalingReq(vedtak, sak))
        }
    }
}
