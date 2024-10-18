package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo

class RammevedtakServiceImpl(
    private val vedtakRepo: RammevedtakRepo,
) : RammevedtakService {

    override fun hentVedtak(vedtakId: VedtakId): Rammevedtak? = vedtakRepo.hentForVedtakId(vedtakId)

    override fun hentVedtakForFnr(
        fnr: Fnr,
        periode: Periode,
        systembruker: Systembruker,
    ): List<Rammevedtak> {
        require(systembruker.roller.harRolle(Rolle.HENTE_DATA)) { "Systembruker mangler rollen HENTE_DATA. Systembrukers roller: ${systembruker.roller}" }
        return vedtakRepo
            .hentForFnr(fnr)
            .filter { vedtak -> vedtak.periode.overlapperMed(periode) }
    }
}
