package no.nav.tiltakspenger.vedtak.routes

import arrow.core.Either
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.callid.callId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException

private val logger = mu.KotlinLogging.logger {}

internal fun ApplicationCall.parameter(parameterNavn: String): String =
    this.parameters[parameterNavn] ?: throw UgyldigRequestException("$parameterNavn ikke funnet")

internal fun ApplicationCall.correlationId(): CorrelationId {
    return this.callId?.let { CorrelationId(it) } ?: CorrelationId.generate()
}

suspend fun ApplicationCall.withSaksnummer(onRight: suspend (Saksnummer) -> Unit) {
    withValidParam(
        paramName = "saksnummer",
        parse = ::Saksnummer,
        errorMessage = "Ugyldig saksnummer",
        errorCode = "ugyldig_saksnummer",
        onSuccess = onRight,
    )
}

suspend fun ApplicationCall.withSakId(onRight: suspend (SakId) -> Unit) {
    withValidParam(
        paramName = "sakId",
        parse = SakId::fromString,
        errorMessage = "Ugyldig sak id",
        errorCode = "ugyldig_sak_id",
        onSuccess = onRight,
    )
}

suspend fun ApplicationCall.withMeldekortId(onRight: suspend (MeldekortId) -> Unit) {
    withValidParam(
        paramName = "meldekortId",
        parse = MeldekortId::fromString,
        errorMessage = "Ugyldig meldekort id",
        errorCode = "ugyldig_meldekort_id",
        onSuccess = onRight,
    )
}

private suspend fun <T> ApplicationCall.withValidParam(
    paramName: String,
    parse: (String) -> T,
    errorMessage: String,
    errorCode: String,
    onSuccess: suspend (T) -> Unit,
) {
    Either.catch {
        parse(this.parameters[paramName]!!)
    }.fold(
        ifLeft = {
            logger.debug(it) { "Feil ved parsing av parameter $paramName. errorMessage: $errorMessage, errorCode: $errorCode" }
            this.respond400BadRequest(
                melding = errorMessage,
                kode = errorCode,
            )
        },
        ifRight = { onSuccess(it) },
    )
}
