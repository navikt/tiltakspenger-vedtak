package no.nav.tiltakspenger.vedtak.repository.attestering

import kotliquery.Session
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering

interface AttesteringDAO {
    fun lagre(
        attestering: Attestering,
        session: Session,
    ): Attestering
}
