package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo

class StatistikkServiceImpl(
    private val statistikkSakRepo: StatistikkSakRepo,
) : StatistikkService {
    override fun lagreOpprettBehandling(sak: SakDetaljer, behandling: Førstegangsbehandling) {
        val dto = opprettBehandlingMapper(sak, behandling)
        statistikkSakRepo.lagre(dto)
    }
}
