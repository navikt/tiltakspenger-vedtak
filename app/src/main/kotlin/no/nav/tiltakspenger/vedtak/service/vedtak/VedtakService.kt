package no.nav.tiltakspenger.vedtak.service.vedtak

import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId

interface VedtakService {
    fun hentVedtak(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
}
