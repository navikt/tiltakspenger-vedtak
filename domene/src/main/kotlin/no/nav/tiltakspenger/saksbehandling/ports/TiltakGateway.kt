package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak

interface TiltakGateway {
    suspend fun hentTiltak(fnr: Fnr): List<Tiltak>
}
