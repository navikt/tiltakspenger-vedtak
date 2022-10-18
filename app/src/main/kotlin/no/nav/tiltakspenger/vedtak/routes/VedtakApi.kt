package no.nav.tiltakspenger.vedtak.routes

import com.auth0.jwk.UrlJwkProvider
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
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.Role
import no.nav.tiltakspenger.vedtak.RoleName
import no.nav.tiltakspenger.vedtak.routes.person.personRoutes
import no.nav.tiltakspenger.vedtak.service.PersonService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetBrukerProvider
import java.net.URI
import java.util.*

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal fun vedtakApi(
    config: Configuration.TokenVerificationConfig,
    innloggetBrukerProvider: InnloggetBrukerProvider,
    personService: PersonService,
): Application.() -> Unit {
    return {
        install(CallId)
        install(CallLogging) {
            callIdMdc("call-id")
            disableDefaultColors()
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
                personRoutes(innloggetBrukerProvider, personService)
            }
            naisRoutes()
        }
    }
}

fun Application.auth(config: Configuration.TokenVerificationConfig) {

    /*
    val jwkProvider: JwkProvider = JwkProviderBuilder(config.jwksUri)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
     */

    val jwkProviderGammel = UrlJwkProvider(URI(config.jwksUri).toURL())

    install(Authentication) {
        fun AuthenticationConfig.jwt(name: String, realm: String, roles: List<RoleName>? = null) =
            jwt(name) {
                SECURELOG.info { "config : $config" }
                this.realm = realm
                verifier(jwkProviderGammel, config.issuer) {
                    withAudience(config.clientId)
                    acceptLeeway(config.leeway)
                }
                challenge { _, _ ->
                    LOG.info { "verifier feilet" }
                    call.respond(HttpStatusCode.Unauthorized, "Ikke tilgang")
                }
                validate { cred ->
                    LOG.info { "er nå i validate, skal ha preferred_username" }
                    if (cred.getClaim("preferred_username", String::class) == null) {
                        LOG.info { "Fant ikke preferred_username" }
                        return@validate null
                    }
                    LOG.info { "er nå i validate, skal ha NAVident" }
                    if (cred.getClaim("NAVident", String::class) == null) {
                        LOG.info { "Fant ikke NAVident" }
                        return@validate null
                    }

                    val claimedRoles: List<UUID> = cred.getListClaim("groups", UUID::class)
                    val configRoles: List<Role> = config.roles
                    val authorizedRoles = configRoles
                        .filter { roles?.contains(it.name) ?: true }
                        .map { it.objectId }
                    if (claimedRoles.none(authorizedRoles::contains)) {
                        LOG.info { "Fant ikke riktig rolle" }
                        return@validate null
                    }

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
