package no.nav.tiltakspenger.vedtak.routes

import arrow.core.Either
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.vedtak.auth2.MicrosoftEntraIdTokenService
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil
import no.nav.tiltakspenger.vedtak.exceptions.UgyldigRequestException

private val logger = mu.KotlinLogging.logger {}

internal fun ApplicationCall.parameter(parameterNavn: String): String =
    this.parameters[parameterNavn] ?: throw UgyldigRequestException("$parameterNavn ikke funnet")

internal fun ApplicationCall.correlationId(): CorrelationId {
    return this.callId?.let { CorrelationId(it) } ?: CorrelationId.generate()
}

internal suspend fun ApplicationCall.withSaksbehandler(
    tokenService: TokenService,
    block: suspend (Saksbehandler) -> Unit,
) {
    return withBruker(tokenService) {
        if (it is Saksbehandler) {
            block(it)
        } else {
            this.respond403Forbidden(
                melding = "Brukeren er ikke en saksbehandler",
                kode = "ikke_saksbehandler",
            )
        }
    }
}

internal suspend fun ApplicationCall.withSystembruker(
    tokenService: MicrosoftEntraIdTokenService,
    block: suspend (Systembruker) -> Unit,
) {
    return withBruker(tokenService) {
        if (it is Systembruker) {
            block(it)
        } else {
            this.respond403Forbidden(
                melding = "Brukeren er ikke en systembruker",
                kode = "ikke_systembruker",
            )
        }
    }
}

internal suspend fun ApplicationCall.withBruker(
    tokenService: TokenService,
    block: suspend (Bruker) -> Unit,
) {
    val token = getBearerToken() ?: return
    tokenService.validerOgHentBruker(token)
        .fold(
            ifLeft = {
                when (it) {
                    is Valideringsfeil.KunneIkkeHenteJwk -> this.respond500InternalServerError(
                        melding = "Feil ved henting av JWK. Denne requesten kan prøves på nytt.",
                        kode = "feil_ved_henting_av_jwk",
                    )

                    is Valideringsfeil.UgyldigToken -> this.respond401Unauthorized(
                        melding = "Ugyldig token. Se tiltakspenger-vedtak sine logger for mer detaljer.",
                        kode = "ugyldig_token",
                    )

                    is Valideringsfeil.UkjentFeil -> this.respond500InternalServerError(
                        melding = "Ukjent feil ved validering av token. Meld fra til #tiltakspenger-værsågod",
                        kode = "ukjent_feil_ved_validering_av_token",
                    )
                }
            },
            ifRight = { block(it) },
        )
}

internal suspend fun ApplicationCall.getBearerToken(): String? {
    val authHeader = request.headers["Authorization"] ?: return respondWithChallenge()
    if (!authHeader.startsWith("Bearer ")) {
        return respondWithChallenge()
    }
    return authHeader.substring(7)
}

private suspend fun ApplicationCall.respondWithChallenge(): String? {
    this.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge("Bearer", "tiltakspenger-vedtak")))
    return null
}

suspend fun ApplicationCall.withSaksnummer(onRight: suspend (Saksnummer) -> Unit) =
    withValidParam(
        paramName = "saksnummer",
        parse = ::Saksnummer,
        errorMessage = "Ugyldig saksnummer",
        errorCode = "ugyldig_saksnummer",
        onSuccess = onRight,
    )

suspend fun ApplicationCall.withSakId(onRight: suspend (SakId) -> Unit) =
    withValidParam(
        paramName = "sakId",
        parse = SakId::fromString,
        errorMessage = "Ugyldig sak id",
        errorCode = "ugyldig_sak_id",
        onSuccess = onRight,
    )

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
