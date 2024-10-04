package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import arrow.core.Either
import arrow.core.right
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.personklient.pdl.KunneIkkeGjøreTilgangskontroll
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService

class TilgangsstyringFakeGateway : TilgangsstyringService {
    private val data = Atomic(mutableMapOf<Fnr, List<AdressebeskyttelseGradering>>())

    fun lagre(
        fnr: Fnr,
        adressebeskyttelseGradering: List<AdressebeskyttelseGradering>,
    ) {
        data.get()[fnr] = adressebeskyttelseGradering
    }
    override suspend fun adressebeskyttelseEnkel(fnr: Fnr): Either<KunneIkkeGjøreTilgangskontroll, List<AdressebeskyttelseGradering>?> {
        return data.get()[fnr]!!.right()
    }

    override suspend fun harTilgangTilPerson(
        fnr: Fnr,
        roller: Roller,
        correlationId: CorrelationId,
    ): Either<KunneIkkeGjøreTilgangskontroll, Boolean> {
        return data.get()[fnr]!!.all {
            when (it) {
                AdressebeskyttelseGradering.FORTROLIG -> roller.harFortroligAdresse()
                AdressebeskyttelseGradering.STRENGT_FORTROLIG, AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND -> roller.harStrengtFortroligAdresse()
                AdressebeskyttelseGradering.UGRADERT -> true
            }
        }.right()
    }
}
