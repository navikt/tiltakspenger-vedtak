package no.nav.tiltakspenger.vedtak.repository.innsending

import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

class InMemoryInnsendingRepository : InnsendingRepository {

    private val søkere = mutableMapOf<String, Innsending>()

    override fun hent(journalpostId: String): Innsending? = søkere[journalpostId]

    override fun lagre(innsending: Innsending): Innsending = with(søkere) {
        this[innsending.journalpostId] = innsending
        return innsending
    }

    override fun findBySøknadId(søknadId: String): Innsending? {
        return null
    }

    override fun findByIdent(ident: String): List<Innsending> = emptyList()

    fun reset() = søkere.clear()
}
