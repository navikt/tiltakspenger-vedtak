package no.nav.tiltakspenger.vedtak.routes

import io.ktor.server.application.ApplicationCall
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException

fun ApplicationCall.parameter(parameterNavn: String): String =
    this.parameters[parameterNavn] ?: throw UgyldigRequestException("$parameterNavn ikke funnet")
