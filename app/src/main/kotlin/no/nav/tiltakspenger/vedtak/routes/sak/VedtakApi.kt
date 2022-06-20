package no.nav.tiltakspenger.vedtak.routes.sak

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import java.net.URI

class TokenVerificationConfig(
    val jwksUri: String,
    val issuer: String,
    val clientId: String,
    val leeway: Long
) {
    companion object {
        fun fromEnv(): TokenVerificationConfig {
            return TokenVerificationConfig(
                jwksUri = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI"),
                issuer = System.getenv("AZURE_OPENID_CONFIG_ISSUER"),
                clientId = System.getenv("AZURE_APP_CLIENT_ID"),
                leeway = 1000
            )
        }
    }
}

internal fun vedtakApi(config: TokenVerificationConfig): Application.() -> Unit {
    val jwkProvider: JwkProvider = UrlJwkProvider(URI(config.jwksUri).toURL())
    return {
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(jwkProvider, config.issuer) {
                    withAudience(config.clientId)
                    acceptLeeway(config.leeway)
                }
                validate { jwtCredential -> JWTPrincipal(jwtCredential.payload) }
            }
        }
        routing {
            authenticate("auth-jwt") {
                sakRoutes()
            }
        }
    }
}
