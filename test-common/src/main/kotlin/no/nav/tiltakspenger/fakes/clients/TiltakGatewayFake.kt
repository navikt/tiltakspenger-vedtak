package no.nav.tiltakspenger.fakes.clients

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway

class TiltakGatewayFake(
    private val data: Map<Fnr, List<Tiltak>>,
) : TiltakGateway {
    override suspend fun hentTiltak(fnr: Fnr): List<Tiltak> {
        return data[fnr]!!
    }
}
