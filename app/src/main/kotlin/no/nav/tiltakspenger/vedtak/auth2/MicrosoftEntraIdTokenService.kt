package no.nav.tiltakspenger.vedtak.auth2

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwk.NetworkException
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.KunneIkkeDekodeToken
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.KunneIkkeVerifisereToken
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.ManglerClaim
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.OidOgSubjectErUlike
import no.nav.tiltakspenger.vedtak.auth2.Valideringsfeil.UgyldigToken.UlikKid
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit
import kotlin.collections.mapNotNull

private val logger = KotlinLogging.logger { }

/**
 * Ansvarlig for å validere token og hente brukerinformasjon.
 * I førsteomgang kun Microsoft Entra Id-token.
 * Dersom den skal utvides til å støtte flere token-typer, put det i egne filer og lag en ny felles abstraksjon.
 *
 * @param url URL til JWKS-endepunkt - Se https://doc.nais.io/auth/explanations/#signature-validation
 * @param issuer utsteder av token - se https://doc.nais.io/auth/explanations/#signature-validation
 * @param clientId vår klient-id - se https://doc.nais.io/auth/explanations/#signature-validation
 * @param acceptIssuedAtLeeway Merk at denne er i sekunder. Kan overstyres for tester.
 * @param acceptNotBeforeLeeway Merk at denne er i sekunder. Kan overstyres for tester.
 */
internal class MicrosoftEntraIdTokenService(
    url: String,
    private val issuer: String,
    private val clientId: String,
    private val autoriserteBrukerroller: List<AdRolle>,
    private val autoriserteSystemtokenroller: Roller = Roller(listOf(Rolle.LAGE_HENDELSER, Rolle.HENTE_DATA)),
    private val acceptIssuedAtLeeway: Long = 5,
    private val acceptNotBeforeLeeway: Long = 5,
) : TokenService {

    // See: https://github.com/auth0/jwks-rsa-java
    private val provider = JwkProviderBuilder(URI(url).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    /**
     * Validerer token og henter brukerinformasjon.
     * Vi fjerner alle roller som ikke er forhåndsgodkjent i [autoriserteBrukerroller] og [autoriserteSystemtokenroller].
     */
    override suspend fun validerOgHentBruker(token: String): Either<Valideringsfeil, Bruker> {
        return Either.catch {
            val decoded: DecodedJWT = Either.catch {
                JWT.decode(token)!!
            }.getOrElse {
                logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: kunne ikke dekode JWT-token. Se sikkerlogg for mer kontekst." }
                sikkerlogg.debug(it) { "token-validering: kunne ikke dekode JWT-token. Token: $token" }
                return KunneIkkeDekodeToken.left()
            }

            val jwk = Either.catch {
                val keyId: String = decoded.keyId!!
                withContext(Dispatchers.IO) {
                    provider.get(keyId)!!
                }
            }.getOrElse {
                return handleJwkError(it, decoded, token)
            }
            val verified = Either.catch {
                // https://doc.nais.io/auth/explanations/#signature-validation
                val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(clientId)
                    .acceptIssuedAt(acceptIssuedAtLeeway)
                    .acceptNotBefore(acceptNotBeforeLeeway)
                    .build()
                    .verify(decoded)
            }.getOrElse {
                logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Feil under verifisering av JWT. Se sikkerlogg for mer kontekst." }
                sikkerlogg.debug(it) { "token-validering: Feil under verifisering av JWT. Token: $token" }
                return KunneIkkeVerifisereToken.left()
            }
            val exp = decoded.getClaim("exp").asLong()!!
            if (((exp * 1000) - System.currentTimeMillis()) < 1000) {
                logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Tokenet er snart utgått. Dette er en sikkerhetsmekanisme for å unngå at tokenet utgår mens vi bruker det. exp: $exp. Se sikkerlogg for mer kontekst." }
                sikkerlogg.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Tokenet er snart utgått. Dette er en sikkerhetsmekanisme for å unngå at tokenet utgår mens vi bruker det. exp: $exp. Token: $token" }
                return KunneIkkeVerifisereToken.left()
            }

            // Forventer at denne kun er satt for systembruker: https://learn.microsoft.com/en-us/entra/identity-platform/optional-claims-reference
            return if (verified.getClaim("idtyp").asString() == "app") {
                verified.hentSystembruker()
            } else {
                verified.hentSaksbehander()
            }
        }.getOrElse {
            // Dette er bare en safe-guard. Dersom det dukker opp feil vi kan håndtere annerledes her, bør vi fange de opp i egne catch-blokker.
            logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Ukjent feil ved validering av token. Se sikkerlogg for mer kontekst." }
            sikkerlogg.error(it) { "token-validering: Ukjent feil ved validering av token. Raw token: $token" }
            Valideringsfeil.UkjentFeil.left()
        }
    }

    private fun DecodedJWT.hentSystembruker(): Either<Valideringsfeil, Systembruker> {
        validerOidOgSubject().getOrElse { return it.left() }

        val brukernavn = this.getClaimAsString("azp_name").getOrElse { return it.left() }

        // I denne klassen prøver vi kun å identifisere feil ved tokenet eller om tokenet er utgått. Hvis tokenet mangler roller, er det en 403 feil og håndteres i ressursene.
        val roller = this
            .getClaim("roles")
            .asList<String>(String::class.java)
            ?.mapNotNull {
                autoriserteSystemtokenroller.find { autorisertRolle ->
                    it.lowercase() == autorisertRolle.toString().lowercase()
                }
            }
            ?.let { Roller(it) }
            ?: run {
                logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Fant ikke claim 'roles' i token." }
                sikkerlogg.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Fant ikke claim 'roles' i token. Dekodet token: ${this.token}. Raw token: $token" }
                return ManglerClaim("roles").left()
            }

        logger.debug { "token-validering: Token validering OK for systembruker $brukernavn med roller $roller" }
        return Systembruker(
            brukernavn = brukernavn,
            roller = roller,
        ).right()
    }

    private fun DecodedJWT.hentSaksbehander(): Either<Valideringsfeil, Saksbehandler> {
        val navIdent: String = this.getClaimAsString("NAVident").getOrElse { return it.left() }
        val epost: String = this.getClaimAsString("preferred_username").getOrElse { return it.left() }

        // I denne klassen prøver vi kun å identifisere feil ved tokenet eller om tokenet er utgått. Hvis tokenet mangler roller, er det en 403 feil og håndteres i ressursene.
        val roller = this
            .getClaim("groups")
            .asList<String>(String::class.java)
            ?.mapNotNull {
                autoriserteBrukerroller.find { autorisertRolle -> it == autorisertRolle.objectId.toString() }?.name
            }?.let { Roller(it) }
            ?: run {
                logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Fant ikke claim 'groups' i token." }
                sikkerlogg.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Fant ikke claim 'groups' i token. Dekodet token: ${this.token}. Raw token: $token" }
                return ManglerClaim("groups").left()
            }

        logger.debug { "token-validering: Token validering OK for saksbehandler $navIdent med roller $roller" }
        // TODO post-mvp jah: Siden vi bruker 2.0, kan vi plukke ut 'family_name' og 'given_name' dersom 'profile' scope er satt i tiltakspenger-saksbehandling for å ha et backupnavn dersom Microsoft graph api eller nom-kallene feiler. Merk, de må være nullable. Se: https://learn.microsoft.com/en-us/entra/identity-platform/optional-claims-reference
        return Saksbehandler(
            navIdent = navIdent,
            brukernavn = epostToBrukernavn(epost),
            epost = epost,
            roller = roller,
        ).right()
    }

    private fun epostToBrukernavn(epost: String): String = epost.split("@").first().replace(".", " ")

    private fun handleJwkError(
        throwable: Throwable,
        decoded: DecodedJWT,
        token: String,
    ): Either<Valideringsfeil, Bruker> {
        return when (throwable) {
            is NetworkException -> {
                logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Nettverksfeil ved henting av JWK. Se sikkerlogg for mer kontekst." }
                sikkerlogg.error(throwable) { "token-validering: Nettverksfeil ved henting av JWK. Dekodet token: ${decoded.token}. Raw token: $token" }
                Valideringsfeil.KunneIkkeHenteJwk.left()
            }

            is SigningKeyNotFoundException -> {
                // Vi ønsker skille på serverfeil vs. klientfeil.
                val message = throwable.message
                return when {
                    message?.contains("No keys found") == true || message?.contains("Failed to parse jwk") == true -> {
                        logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Feil ved henting/parsing av JWK. Se sikkerlogg for mer kontekst." }
                        sikkerlogg.error(throwable) { "token-validering: Feil ved henting/parsing av JWK. Dekodet token: ${decoded.token}. Raw token: $token" }
                        Valideringsfeil.KunneIkkeHenteJwk.left()
                    }

                    message?.contains("No key found") == true -> {
                        logger.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Kunne ikke knytte JWK sin kid til JWT sin kid. Se sikkerlogg for mer kontekst." }
                        sikkerlogg.debug(throwable) { "token-validering: Kunne ikke knytte JWK sin kid til JWT sin kid. Dekodet token: ${decoded.token}. Raw token: $token" }
                        UlikKid.left()
                    }
                    // Fanges opp lengre ned som ukjent feil.
                    else -> throw throwable
                }
            }
            // Fanges opp lengre ned som ukjent feil.
            else -> throw throwable
        }
    }
}

/** Vi prøver skille mellom 3 hovedtyper feil: klientfeil, serverfeil og ukjente feil. */
sealed interface Valideringsfeil {
    sealed interface UgyldigToken : Valideringsfeil {
        /** Brukes i hovedsak for enklere testing */
        data class ManglerClaim(val claim: String) : UgyldigToken
        data class OidOgSubjectErUlike(val oid: String, val sub: String) : UgyldigToken
        data object UlikKid : UgyldigToken
        data object KunneIkkeDekodeToken : UgyldigToken
        data object KunneIkkeVerifisereToken : UgyldigToken
    }

    data object KunneIkkeHenteJwk : Valideringsfeil
    data object UkjentFeil : Valideringsfeil
}

private fun DecodedJWT.getClaimAsString(name: String): Either<UgyldigToken, String> {
    return this.getClaim(name).asString()?.nullIfBlank()?.right() ?: run {
        logger.debug { "token-validering: Fant ikke claim '$name' i token." }
        sikkerlogg.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: Fant ikke claim '$name' i token. Dekodet token: ${this.token}" }
        ManglerClaim(name).left()
    }
}

private fun DecodedJWT.validerOidOgSubject(): Either<UgyldigToken, Unit> {
    val oid = this.getClaimAsString("oid").getOrElse { return it.left() }
    val sub = this.getClaimAsString("sub").getOrElse { return it.left() }
    if (oid != sub) {
        logger.debug { "token-validering: oid ($oid) er ulik sub ($sub). Se sikkerlogg for mer kontekst." }
        sikkerlogg.debug(RuntimeException("Trigger stacktrace for enklere debug.")) { "token-validering: oid ($oid) er ulik sub ($sub). Dekodet token: ${this.token}" }
        return OidOgSubjectErUlike(oid, sub).left()
    }
    return Unit.right()
}

private fun String?.nullIfBlank(): String? {
    return if (this.isNullOrBlank()) null else this
}
