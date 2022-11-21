package no.nav.tiltakspenger.vedtak.repository.søker

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class InMemorySøkerRepository : SøkerRepository {

    private val søkere = mutableMapOf<String, Søker>()

    override fun hent(ident: String): Søker? = søkere[ident]
    override fun hentBySøkerId(søkerId: SøkerId) = søkere[søkerId.toString()]

    override fun lagre(søker: Søker) = with(søkere) {
        this[søker.ident] = søker
    }

    override fun findBySøknadId(søknadId: String): Søker? {
        return null
    }

    fun reset() = søkere.clear()
}
