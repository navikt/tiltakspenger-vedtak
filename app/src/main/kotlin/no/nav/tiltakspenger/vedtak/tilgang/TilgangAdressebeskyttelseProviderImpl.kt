package no.nav.tiltakspenger.vedtak.tilgang

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.adressebeskyttelse.FellesAdressebeskyttelseKlient

class TilgangAdressebeskyttelseProviderImpl(
    val fellesAdressebeskyttelseKlient: FellesAdressebeskyttelseKlient,
) : TilgangAdressebeskyttelseProvider {

    override suspend fun sjekkTilgangEnkel(fnr: Fnr) {
        fellesAdressebeskyttelseKlient.enkel(fnr)
    }

    override suspend fun sjekkTilgangBolk(fnrListe: List<Fnr>) {
        fellesAdressebeskyttelseKlient.bolk(fnrListe)
    }
}
