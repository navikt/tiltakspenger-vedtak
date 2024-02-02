package no.nav.tiltakspenger.vedtak.service.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId

interface VedtakService {
    fun hentVedtak(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
    suspend fun lagVedtakForBehandling(behandling: BehandlingIverksatt, tx: TransactionalSession): Vedtak
}
