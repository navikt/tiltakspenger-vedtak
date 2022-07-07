package no.nav.tiltakspenger.vedtak.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
import java.net.URI

internal fun vedtakApi(config: TokenVerificationConfig): Application.() -> Unit {
    return {
        openAPI()
        jacksonSerialization()
        auth(config)
        routing {
            apiRouting {
                auth {
                    personRoutes()
                }
            }
            get("/openapi.json") {
                call.respond(this@routing.application.openAPIGen.api.serialize())
            }
            get("/") {
                call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
            }
        }
    }
}

fun Application.auth(config: TokenVerificationConfig) {
    val jwkProvider: JwkProvider = UrlJwkProvider(URI(config.jwksUri).toURL())
    install(Authentication) {
        jwt {
            verifier(jwkProvider, config.issuer) {
                withAudience(config.clientId)
                acceptLeeway(config.leeway)
            }
            validate { jwtCredential -> JWTPrincipal(jwtCredential.payload) }
        }
    }
}

fun Application.jacksonSerialization() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }
    }
}

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
