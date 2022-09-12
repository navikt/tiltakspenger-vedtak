package no.nav.tiltakspenger.vedtak.repository.søker

import no.nav.tiltakspenger.vedtak.Søker

interface SøkerRepository {
    fun hent(ident: String): Søker?
    fun lagre(søker: Søker): Int
    fun oppdaterTilstand(tilstand: Søker.Tilstand): Unit
}
