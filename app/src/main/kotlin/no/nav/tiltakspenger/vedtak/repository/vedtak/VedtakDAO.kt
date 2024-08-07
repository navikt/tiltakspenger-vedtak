package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakDAO {
    fun lagreVedtak(
        vedtak: Vedtak,
        tx: TransactionalSession,
    ): Vedtak

    fun hentVedtakForSak(
        sakId: SakId,
        sessionContext: SessionContext,
    ): List<Vedtak>
}
