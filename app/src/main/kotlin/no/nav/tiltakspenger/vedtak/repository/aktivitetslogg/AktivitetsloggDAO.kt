package no.nav.tiltakspenger.vedtak.repository.aktivitetslogg

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import java.util.*

class AktivitetsloggDAO {
    fun lagre(søkerId: UUID, aktivitetslogg: Aktivitetslogg, txSession: TransactionalSession) {

    }

    fun hent(søkerId: UUID, txSession: TransactionalSession): Aktivitetslogg {
        return Aktivitetslogg()
    }
}
