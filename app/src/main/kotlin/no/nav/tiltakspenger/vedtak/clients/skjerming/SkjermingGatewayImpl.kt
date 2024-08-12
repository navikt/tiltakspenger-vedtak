package no.nav.tiltakspenger.vedtak.clients.skjerming

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.skjerming.SkjermingClient
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway

class SkjermingGatewayImpl(
    private val skjermingClient: SkjermingClient,
) : SkjermingGateway {
    override suspend fun erSkjermetPerson(fnr: Fnr): Boolean = skjermingClient.erSkjermetPerson(fnr)
}
