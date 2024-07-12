package no.nav.tiltakspenger.vedtak.clients.skjerming

interface SkjermingClient {
    suspend fun erSkjermetPerson(fødselsnummer: String): Boolean
}
