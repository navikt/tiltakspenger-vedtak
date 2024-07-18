package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface StatistikkService {
    fun lagreOpprettBehandling(sak: SakDetaljer, behandling: Førstegangsbehandling)
    fun lagreStønadstatistikk(sak: SakDetaljer, vedtak: Vedtak)
}
