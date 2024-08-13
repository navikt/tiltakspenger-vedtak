package no.nav.tiltakspenger.vedtak.clients.skjerming

import no.nav.tiltakspenger.libs.common.Fnr

interface SkjermingClient {
    suspend fun erSkjermetPerson(fnr: Fnr): Boolean
}
