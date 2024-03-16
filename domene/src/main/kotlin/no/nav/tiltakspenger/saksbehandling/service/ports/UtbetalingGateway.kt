package no.nav.tiltakspenger.saksbehandling.service.ports

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface UtbetalingGateway {
    fun iverksett(vedtak: Vedtak, sak: SakDetaljer): String
}
