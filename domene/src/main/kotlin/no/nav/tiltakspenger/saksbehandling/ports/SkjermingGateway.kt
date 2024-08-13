package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr

interface SkjermingGateway {
    suspend fun erSkjermetPerson(fnr: Fnr): Boolean
}
