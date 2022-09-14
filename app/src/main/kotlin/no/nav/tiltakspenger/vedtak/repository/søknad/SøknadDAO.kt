package no.nav.tiltakspenger.vedtak.repository.søknad

import no.nav.tiltakspenger.vedtak.Søknad

interface SøknadDAO {
    fun hentAlle(ident: String): List<Søknad>
    fun lagre(ident: String, søknader: List<Søknad>): Int
}
