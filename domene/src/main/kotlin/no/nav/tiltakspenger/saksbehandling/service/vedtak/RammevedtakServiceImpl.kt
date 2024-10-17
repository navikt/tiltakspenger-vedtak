package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.libs.common.Fnr
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
    ): List<Rammevedtak> {
        return vedtakRepo
            .hentForFnr(fnr)
            .filter { vedtak -> vedtak.periode.overlapperMed(periode) }
    }
}
