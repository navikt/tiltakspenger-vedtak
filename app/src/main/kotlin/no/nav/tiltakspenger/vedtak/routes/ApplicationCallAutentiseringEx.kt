package no.nav.tiltakspenger.vedtak.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.auth2.TokenService
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil
import kotlin.text.startsWith
import kotlin.text.substring

internal suspend inline fun ApplicationCall.withSaksbehandler(
    tokenService: TokenService,
    crossinline block: suspend (Saksbehandler) -> Unit,
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

internal suspend inline fun ApplicationCall.withSystembruker(
    tokenService: TokenService,
    crossinline block: suspend (Systembruker) -> Unit,
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

internal suspend inline fun ApplicationCall.withBruker(
    tokenService: TokenService,
    crossinline block: suspend (Bruker) -> Unit,
) {
    val token = getBearerToken() ?: return
    tokenService.validerOgHentBruker(token)
        .onLeft {
            when (it) {
                is Valideringsfeil.KunneIkkeHenteJwk -> this.respond500InternalServerError(
                    melding = "Feil ved henting av JWK. Denne requesten kan prøves på nytt.",
                    kode = "feil_ved_henting_av_jwk",
                )

                is Valideringsfeil.UgyldigToken -> this.respond401Unauthorized(
                    melding = "Ugyldig token. Se tiltakspenger-saksbehandling-api sine logger for mer detaljer.",
                    kode = "ugyldig_token",
                )

                is Valideringsfeil.UkjentFeil -> this.respond500InternalServerError(
                    melding = "Ukjent feil ved validering av token. Meld fra til #tiltakspenger-værsågod",
                    kode = "ukjent_feil_ved_validering_av_token",
                )
            }
        }
        .onRight {
            if (it.roller.isEmpty()) {
                this.respond403Forbidden(
                    melding = "Brukeren må ha minst en autorisert rolle for å aksessere denne ressursen",
                    kode = "mangler_rolle",
                )
            } else {
                block(it)
            }
        }
}

internal suspend fun ApplicationCall.getBearerToken(): String? {
    val authHeader = request.headers["Authorization"] ?: return respondWithChallenge()
    if (!authHeader.startsWith("Bearer ")) {
        return respondWithChallenge()
    }
    return authHeader.substring(7)
}

private suspend fun ApplicationCall.respondWithChallenge(): String? {
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/WWW-Authenticate
    this.response.headers.append("WWW-Authenticate", "Bearer realm=\"tiltakspenger-saksbehandling-api\"")
    this.respond(HttpStatusCode.Unauthorized)
    return null
}
