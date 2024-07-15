package no.nav.tiltakspenger.vedtak.clients.skjerming

import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway

class SkjermingGatewayImpl(
    private val skjermingClient: SkjermingClient,
) : SkjermingGateway {
    override suspend fun erSkjermetPerson(ident: String): Boolean {
        return skjermingClient.erSkjermetPerson(ident)
    }
}
