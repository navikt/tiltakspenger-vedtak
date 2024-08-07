package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.Session
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering

interface AttesteringDAO {
    fun lagre(
        attestering: Attestering,
        session: Session,
    ): Attestering
}
