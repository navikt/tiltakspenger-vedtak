package no.nav.tiltakspenger.vedtak.auth2

import arrow.core.Either
import no.nav.tiltakspenger.felles.Bruker

interface TokenService {
    suspend fun validerOgHentBruker(token: String): Either<Valideringsfeil, Bruker>
}
