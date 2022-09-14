package no.nav.tiltakspenger.vedtak.repository.søknad

import java.util.*
import no.nav.tiltakspenger.vedtak.Søknad

interface SøknadDAO {
    fun hentAlle(søkerId: UUID): List<Søknad>
    fun lagre(søkerId: UUID, søknader: List<Søknad>)
}
