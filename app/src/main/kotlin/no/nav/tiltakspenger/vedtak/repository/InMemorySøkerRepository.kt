package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository

class InMemorySøkerRepository : SøkerRepository {

    private val søkere = mutableMapOf<String, Søker>()

    override fun hent(ident: String): Søker? = søkere[ident]

    override fun lagre(søker: Søker): Int = with(søkere) {
        this[søker.ident] = søker
        this.size
    }

    fun reset() = søkere.clear()
}
