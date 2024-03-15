package no.nav.tiltakspenger.vedtak.service.ports

import no.nav.tiltakspenger.domene.sak.SakDetaljer
import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface UtbetalingGateway {
    suspend fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String
}
