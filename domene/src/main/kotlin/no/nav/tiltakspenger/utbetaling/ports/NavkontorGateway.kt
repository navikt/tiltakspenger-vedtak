package no.nav.tiltakspenger.utbetaling.ports

import arrow.core.Either
import no.nav.tiltakspenger.felles.GeografiskOmråde
import no.nav.tiltakspenger.felles.Navkontor

interface NavkontorGateway {
    suspend fun hentNavkontor(geografiskOmråde: GeografiskOmråde): Either<KunneIkkeHenteNavkontor, Navkontor>
}

data object KunneIkkeHenteNavkontor
