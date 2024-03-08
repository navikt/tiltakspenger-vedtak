package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.vedtak.Vedtak

interface VedtakRepo {
    fun hent(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
    fun hentVedtakForSak(sakId: SakId): List<Vedtak>
    fun lagreVedtak(vedtak: Vedtak): Vedtak
    fun lagreVedtak(vedtak: Vedtak, tx: TransactionalSession): Vedtak
}
