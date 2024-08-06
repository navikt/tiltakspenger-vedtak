package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering

interface AttesteringDAO {
    fun lagre(attestering: Attestering, tx: TransactionalSession): Attestering
}
