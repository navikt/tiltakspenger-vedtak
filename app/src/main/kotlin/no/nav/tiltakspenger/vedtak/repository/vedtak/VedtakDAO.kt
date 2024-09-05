package no.nav.tiltakspenger.vedtak.repository.vedtak

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface VedtakDAO {
    fun lagreVedtak(
        vedtak: Rammevedtak,
        tx: TransactionalSession,
    )

    fun hentVedtakForSak(
        sakId: SakId,
        sessionContext: SessionContext,
    ): List<Rammevedtak>
}
