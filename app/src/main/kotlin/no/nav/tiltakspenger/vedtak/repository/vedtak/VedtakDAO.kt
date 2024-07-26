package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakDAO {
    fun lagreVedtak(vedtak: Vedtak, tx: TransactionalSession): Vedtak
    fun hentVedtakForSak(sakId: SakId, tx: TransactionalSession): List<Vedtak>
}
