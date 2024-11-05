package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.TiltakDTO

interface TiltakClient {
    suspend fun hentTiltak(fnr: Fnr, correlationId: CorrelationId): List<TiltakDTO>
}
