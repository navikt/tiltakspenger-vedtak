package no.nav.tiltakspenger.saksbehandling.ports

interface SkjermingGateway {
    suspend fun erSkjermetPerson(ident: String): Boolean
}
