package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import java.time.LocalDateTime

class TiltakGatewayImpl(
    private val tiltakClient: TiltakClient,
) : TiltakGateway {
    override suspend fun hentTiltak(ident: String): List<Tiltak> {
        return mapTiltak(tiltakClient.hentTiltak(ident), LocalDateTime.now())
    }
}
