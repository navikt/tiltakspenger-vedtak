package no.nav.tiltakspenger.vedtak.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.callid.callId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException

fun ApplicationCall.parameter(parameterNavn: String): String =
    this.parameters[parameterNavn] ?: throw UgyldigRequestException("$parameterNavn ikke funnet")

fun ApplicationCall.correlationId(): CorrelationId {
    return this.callId ?.let { CorrelationId(it) } ?: CorrelationId.generate()
}
