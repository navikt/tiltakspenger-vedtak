package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingDAO {
    fun lagre(behandling: Førstegangsbehandling, tx: TransactionalSession): Førstegangsbehandling
}
