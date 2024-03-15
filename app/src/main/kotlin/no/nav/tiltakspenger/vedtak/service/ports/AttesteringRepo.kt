package no.nav.tiltakspenger.vedtak.service.ports

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.felles.BehandlingId

interface AttesteringRepo {
    fun lagre(attestering: Attestering): Attestering
    fun lagre(attestering: Attestering, tx: TransactionalSession): Attestering
    fun hentForBehandling(behandlingId: BehandlingId): List<Attestering>
}
