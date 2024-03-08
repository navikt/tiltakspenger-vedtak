package no.nav.tiltakspenger.vedtak.service.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.vedtak.Vedtak

interface VedtakService {
    fun hentVedtak(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
    suspend fun lagVedtakForBehandling(behandling: BehandlingIverksatt, tx: TransactionalSession): Vedtak
}
