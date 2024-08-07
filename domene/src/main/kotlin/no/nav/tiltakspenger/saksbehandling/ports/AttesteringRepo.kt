package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering

interface AttesteringRepo {
    fun lagre(
        attestering: Attestering,
        context: TransactionContext? = null,
    ): Attestering

    fun hentForBehandling(behandlingId: BehandlingId): List<Attestering>
}
