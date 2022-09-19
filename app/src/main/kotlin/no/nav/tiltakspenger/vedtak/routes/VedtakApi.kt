package no.nav.tiltakspenger.vedtak.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.RoleName
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import java.util.*
import java.util.concurrent.TimeUnit

internal fun vedtakApi(
    config: Configuration.TokenVerificationConfig,
    innloggetBrukerProvider: InnloggetBrukerProvider
): Application.() -> Unit {
    return {
        install(CallId)
        install(CallLogging) {
            callIdMdc("call-id")
            filter { call ->
                !call.request.path().startsWith("/isalive") &&
                        !call.request.path().startsWith("/isready") &&
                        !call.request.path().startsWith("/metrics")
            }
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
