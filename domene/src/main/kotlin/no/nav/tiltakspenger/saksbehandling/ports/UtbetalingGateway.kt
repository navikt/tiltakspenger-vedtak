package no.nav.tiltakspenger.saksbehandling.ports

import arrow.core.Either
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak

interface UtbetalingGateway {
    suspend fun iverksett(
        vedtak: Utbetalingsvedtak,
        forrigeUtbetalingJson: String?,
        correlationId: CorrelationId,
    ): Either<KunneIkkeUtbetale, SendtUtbetaling>
}

object KunneIkkeUtbetale

data class SendtUtbetaling(
    val request: String,
    val response: String,
    val responseStatus: Int,
)
