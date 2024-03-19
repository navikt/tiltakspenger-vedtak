package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling

interface BehandlingDAO {
    fun lagre(behandling: Behandling, tx: TransactionalSession): Behandling
}
