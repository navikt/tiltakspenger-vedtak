package no.nav.tiltakspenger.vedtak.clients.utbetaling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

object UtbetalingReqMapper {

    fun mapUtbetalingReq(vedtak: Vedtak, sak: SakDetaljer): UtbetalingDTO {
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
