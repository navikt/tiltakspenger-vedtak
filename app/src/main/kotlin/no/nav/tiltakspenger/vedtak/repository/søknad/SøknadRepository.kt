package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.Session
import no.nav.tiltakspenger.vedtak.Søknad
import java.util.UUID

interface SøknadRepository {
    fun hentAlle(ident: String, session: Session): List<Søknad>
    fun hent(søknadId: UUID): Søknad?
    fun lagre(søknader: List<Søknad>): Unit
}
