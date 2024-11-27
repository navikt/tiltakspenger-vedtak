package no.nav.tiltakspenger.datadeling.ports

import arrow.core.Either
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface DatadelingGateway {
    suspend fun send(
        rammevedtak: Rammevedtak,
        correlationId: CorrelationId,
    ): Either<FeilVedSendingTilDatadeling, Unit>
    suspend fun send(
        behandling: Behandling,
        correlationId: CorrelationId,
    ): Either<FeilVedSendingTilDatadeling, Unit>
}

data object FeilVedSendingTilDatadeling
