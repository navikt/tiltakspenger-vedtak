package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface UtbetalingGateway {
    suspend fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String
}
