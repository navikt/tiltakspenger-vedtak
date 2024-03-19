package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering

interface AttesteringDAO {
    fun lagre(attestering: Attestering, tx: TransactionalSession): Attestering
}
