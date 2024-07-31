package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.TiltakDTO

interface TiltakClient {
    suspend fun hentTiltak(fnr: Fnr): List<TiltakDTO>
}
