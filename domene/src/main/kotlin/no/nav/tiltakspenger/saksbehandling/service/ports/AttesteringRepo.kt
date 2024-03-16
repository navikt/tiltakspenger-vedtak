package no.nav.tiltakspenger.saksbehandling.service.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering

interface AttesteringRepo {
    fun lagre(attestering: Attestering): Attestering

    // fun lagre(attestering: Attestering, tx: TransactionalSession): Attestering
    fun hentForBehandling(behandlingId: BehandlingId): List<Attestering>
}
