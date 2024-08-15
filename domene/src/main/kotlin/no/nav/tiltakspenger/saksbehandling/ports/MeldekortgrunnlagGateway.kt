package no.nav.tiltakspenger.saksbehandling.ports

import arrow.core.Either
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface MeldekortgrunnlagGateway {
    suspend fun sendMeldekortGrunnlag(
        vedtak: Vedtak,
        correlationId: CorrelationId,
    ): Either<KunneIkkeSendeMeldekortGrunnlag, Boolean>
}
sealed interface KunneIkkeSendeMeldekortGrunnlag {
    data class NetworkError(val exception: Throwable) : KunneIkkeSendeMeldekortGrunnlag
    data class DeserializationException(val exception: Throwable) : KunneIkkeSendeMeldekortGrunnlag

    /**
     * @param body Bør nok ikke logges til vanlig logg, siden den kan inneholde fødselsnummer.
     */
    data class Ikke2xx(
        val status: Int,
        val body: String?,
    ) : KunneIkkeSendeMeldekortGrunnlag
}
