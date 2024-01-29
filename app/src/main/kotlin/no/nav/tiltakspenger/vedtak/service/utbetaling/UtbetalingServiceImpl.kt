package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.clients.utbetaling.IverksettingResultat
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingClient
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingDTO
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class UtbetalingServiceImpl(
    private val utbetalingClient: UtbetalingClient,
    private val sakRepo: SakRepo,
) : UtbetalingService {
    override suspend fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String {
        return utbetalingClient.iverksett(mapUtbetalingReq(vedtak))
    }

    private fun mapUtbetalingReq(vedtak: Vedtak): UtbetalingDTO {
        val sak = sakRepo.hentKunSak(vedtak.sakId) ?: throw IllegalStateException("Fant ikke sak for vedtak")
        return UtbetalingDTO(
            sakId = sak.id.toString(),
            saksnummer = sak.saknummer.verdi,
            behandlingId = vedtak.behandling.id.toString(),
            personIdent = sak.ident,
            fom = vedtak.periode.fra,
            tom = vedtak.periode.til,
            iverksettingResultat = IverksettingResultat.INNVILGET,
            vedtaktidspunkt = vedtak.vedtaksdato,
            saksbehandler = vedtak.saksbehandler,
            beslutter = vedtak.beslutter,
        )
    }
}
