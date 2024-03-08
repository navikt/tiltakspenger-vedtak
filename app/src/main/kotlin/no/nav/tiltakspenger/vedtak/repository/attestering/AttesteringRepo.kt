package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId

interface AttesteringRepo {
    fun lagre(attestering: no.nav.tiltakspenger.saksbehandling.attestering.Attestering): no.nav.tiltakspenger.saksbehandling.attestering.Attestering
    fun lagre(
        attestering: no.nav.tiltakspenger.saksbehandling.attestering.Attestering,
        tx: TransactionalSession,
    ): no.nav.tiltakspenger.saksbehandling.attestering.Attestering

    fun hentForBehandling(behandlingId: BehandlingId): List<no.nav.tiltakspenger.saksbehandling.attestering.Attestering>
}
