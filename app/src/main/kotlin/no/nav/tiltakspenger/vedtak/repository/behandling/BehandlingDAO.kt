package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Session
import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingDAO {
    fun lagre(behandling: Behandling, tx: TransactionalSession)
    fun hentForSak(sakId: SakId, session: Session): List<Førstegangsbehandling>
}
