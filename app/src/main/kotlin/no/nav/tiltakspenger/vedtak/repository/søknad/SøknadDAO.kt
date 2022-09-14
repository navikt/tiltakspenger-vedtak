package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import kotliquery.TransactionalSession
import no.nav.tiltakspenger.vedtak.Søknad

interface SøknadDAO {
    fun hentAlle(søkerId: UUID, txSession: TransactionalSession): List<Søknad>
    fun lagre(søkerId: UUID, søknader: List<Søknad>, txSession: TransactionalSession)
}
