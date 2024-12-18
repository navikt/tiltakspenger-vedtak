package no.nav.tiltakspenger.meldekort.ports

import arrow.core.Either
import no.nav.tiltakspenger.meldekort.domene.Meldekort

interface MeldekortApiHttpClientGateway {
    suspend fun sendMeldekort(meldekort: Meldekort): Either<FeilVedSendingTilMeldekortApi, Unit>
}

data object FeilVedSendingTilMeldekortApi
