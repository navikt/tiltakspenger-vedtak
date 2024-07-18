package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.opprettBehandlingMapper
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.stønadStatistikkMapper

class StatistikkServiceImpl(
    private val statistikkSakRepo: StatistikkSakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
) : StatistikkService {
    override fun lagreOpprettBehandling(sak: SakDetaljer, behandling: Førstegangsbehandling) {
        val dto = opprettBehandlingMapper(sak, behandling)
        statistikkSakRepo.lagre(dto)
    }

    override fun lagreStønadstatistikk(sak: SakDetaljer, vedtak: Vedtak) {
        val dto = stønadStatistikkMapper(sak, vedtak)
        statistikkStønadRepo.lagre(dto)
    }
}
