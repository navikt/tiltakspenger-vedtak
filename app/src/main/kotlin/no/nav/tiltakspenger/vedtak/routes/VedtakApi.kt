package no.nav.tiltakspenger.vedtak.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.util.*
import java.util.concurrent.TimeUnit
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.RoleName
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider

internal fun vedtakApi(
    config: Configuration.TokenVerificationConfig,
    innloggetBrukerProvider: InnloggetBrukerProvider
): Application.() -> Unit {
    return {
        install(CallId)
        install(CallLogging) {
            callIdMdc("call-id")
        }
        jacksonSerialization()
        auth(config)
        routing {
            authenticate("saksbehandling") {
                personRoutes(innloggetBrukerProvider)
            }
            naisRoutes()
        }
    }
}

fun Application.auth(config: Configuration.TokenVerificationConfig) {

    val jwkProvider: JwkProvider = JwkProviderBuilder(config.jwksUri)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        fun AuthenticationConfig.jwt(name: String, realm: String, roles: List<RoleName>? = null) =
            jwt(name) {
                this.realm = realm
                verifier(jwkProvider, config.issuer) {
                    withAudience(config.clientId)
                    acceptLeeway(config.leeway)
                }
                challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, "Ikke tilgang") }
                validate { cred ->
                    if (cred.getClaim("preferred_username", String::class) == null) return@validate null
                    if (cred.getClaim("NAVident", String::class) == null) return@validate null

                    val claimedRoles = cred.getListClaim("groups", UUID::class)
                    val authorizedRoles = config.roles
                        .filter { roles?.contains(it.name) ?: true }
                        .map { it.objectId }
                    if (claimedRoles.none(authorizedRoles::contains)) return@validate null

                    JWTPrincipal(cred.payload)
                }
            }
        jwt("saksbehandling", "saksbehandling", listOf(RoleName.SAKSBEHANDLER))
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
