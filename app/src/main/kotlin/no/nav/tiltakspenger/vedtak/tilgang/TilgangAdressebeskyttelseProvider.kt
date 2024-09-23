package no.nav.tiltakspenger.vedtak.tilgang

import no.nav.tiltakspenger.libs.common.Fnr

interface TilgangAdressebeskyttelseProvider {
    suspend fun sjekkTilgangEnkel(fnr: Fnr)

    suspend fun sjekkTilgangBolk(fnrListe: List<Fnr>)
}
