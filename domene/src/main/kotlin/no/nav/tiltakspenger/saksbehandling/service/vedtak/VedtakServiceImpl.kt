package no.nav.tiltakspenger.saksbehandling.service.vedtak

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class VedtakServiceImpl(
    private val vedtakRepo: VedtakRepo,
) : VedtakService {
    override fun hentVedtak(vedtakId: VedtakId): Vedtak? = vedtakRepo.hent(vedtakId)

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak = vedtakRepo.hentVedtakForBehandling(behandlingId)
}
