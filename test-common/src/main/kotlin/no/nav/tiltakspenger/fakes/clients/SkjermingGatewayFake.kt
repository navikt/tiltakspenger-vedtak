package no.nav.tiltakspenger.fakes.clients

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway

class SkjermingGatewayFake(
    private val data: Map<Fnr, Boolean>,
) : SkjermingGateway {
    /**
     * Feiler dersom personen ikke finnes i datastettet
     */
    override suspend fun erSkjermetPerson(fnr: Fnr): Boolean {
        return data[fnr]!!
    }
}
