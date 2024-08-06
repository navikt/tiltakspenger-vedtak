package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import java.time.LocalDateTime

class TiltakGatewayImpl(
    private val tiltakClient: TiltakClient,
) : TiltakGateway {
    override suspend fun hentTiltak(fnr: Fnr): List<Tiltak> = mapTiltak(tiltakClient.hentTiltak(fnr), LocalDateTime.now())
}
