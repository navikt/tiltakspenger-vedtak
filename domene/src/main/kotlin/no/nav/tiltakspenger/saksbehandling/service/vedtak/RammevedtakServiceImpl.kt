package no.nav.tiltakspenger.saksbehandling.service.vedtak

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class RammevedtakServiceImpl(
    private val vedtakRepo: RammevedtakRepo,
) : RammevedtakService {
    override fun hentVedtak(vedtakId: VedtakId): Rammevedtak? = vedtakRepo.hent(vedtakId)
}
