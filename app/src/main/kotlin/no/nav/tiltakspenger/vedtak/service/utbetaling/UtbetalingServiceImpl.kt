package no.nav.tiltakspenger.vedtak.service.utbetaling

import no.nav.tiltakspenger.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingClient
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallForPeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallsperiodeDTO
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class UtbetalingServiceImpl(
    private val utbetalingClient: UtbetalingClient,
    private val sakRepo: SakRepo,
) : UtbetalingService {
    override suspend fun sendBehandlingTilUtbetaling(vedtak: Vedtak): String {
        return utbetalingClient.iverksett(mapUtbetalingReq(vedtak))
    }

    private fun mapUtbetalingReq(vedtak: Vedtak): UtbetalingDTO {
        val sak = sakRepo.hentSakDetaljer(vedtak.sakId) ?: throw IllegalStateException("Fant ikke sak for vedtak")
        return UtbetalingDTO(
            sakId = sak.id.toString(),
            utløsendeId = vedtak.behandling.id.toString(),
            ident = sak.ident,
            utfallsperioder = vedtak.utfallsperioder.map {
                UtfallsperiodeDTO(
                    fom = it.fom,
                    tom = it.tom,
                    antallBarn = it.antallBarn,
                    utfall = when (it.utfall) {
                        UtfallForPeriode.GIR_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                        UtfallForPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING
                    },
                )
            },
            brukerNavkontor = "0220", // Denne må hentes fra NORG
            vedtaktidspunkt = vedtak.vedtaksdato,
            saksbehandler = vedtak.saksbehandler,
            beslutter = vedtak.beslutter,
        )
    }
}
