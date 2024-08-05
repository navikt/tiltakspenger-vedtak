package no.nav.tiltakspenger.vedtak.repository.behandling

import arrow.core.NonEmptyList
import kotliquery.Session
import kotliquery.TransactionalSession
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingDAO {
    fun lagre(behandling: Behandling, tx: TransactionalSession)
    fun hentForSak(sakId: SakId, session: Session): NonEmptyList<Førstegangsbehandling>
}
