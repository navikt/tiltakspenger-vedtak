package no.nav.tiltakspenger.vedtak.routes

import com.auth0.jwk.UrlJwkProvider
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.routes.admin.resettInnsendingerRoute
import no.nav.tiltakspenger.vedtak.routes.rivers.personopplysningerRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.skjermingRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.søknadRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.tiltakRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.ytelseRoutes
import no.nav.tiltakspenger.vedtak.routes.saksbehandler.saksbehandlerRoutes
import no.nav.tiltakspenger.vedtak.routes.søker.søkerRoutes
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import no.nav.tiltakspenger.vedtak.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSystembrukerProvider
import java.net.URI
import java.util.*

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal fun Application.vedtakApi(
    config: Configuration.TokenVerificationConfig,
    innloggetSaksbehandlerProvider: JWTInnloggetSaksbehandlerProvider,
    innloggetSystembrukerProvider: JWTInnloggetSystembrukerProvider,
    søkerService: SøkerService,
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
    innsendingAdminService: InnsendingAdminService
) {
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
            søkerRoutes(innloggetSaksbehandlerProvider, søkerService)
            saksbehandlerRoutes(innloggetSaksbehandlerProvider)
        }
        resettInnsendingerRoute(innsendingAdminService)
        authenticate("systemtoken") {
            søknadRoutes(innsendingMediator, søkerMediator)
            skjermingRoutes(innsendingMediator)
            tiltakRoutes(innsendingMediator)
            ytelseRoutes(innsendingMediator)
            personopplysningerRoutes(
                innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                innsendingMediator = innsendingMediator,
                søkerMediator = søkerMediator,
            )
        }
        static("/") {
            staticBasePackage = "static"
            resource("index.html")
            defaultResource("index.html")
        }
    }
}

private fun AuthenticationConfig.jwt(config: Configuration.TokenVerificationConfig, name: String, realm: String, roles: List<Rolle>? = null) =
    jwt(name) {
        SECURELOG.info { "config : $config" }
        this.realm = realm
        val jwkProviderGammel = UrlJwkProvider(URI(config.jwksUri).toURL())
        verifier(jwkProviderGammel, config.issuer) {
            LOG.info { "Er nå i verifier" }
            withAudience(config.clientId)
            acceptLeeway(config.leeway)
        }
        challenge { _, _ ->
            LOG.info { "verifier feilet" }
            call.respond(HttpStatusCode.Unauthorized, "Ikke tilgang")
        }
        validate { cred ->
            SECURELOG.info("Cred er $cred")
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
            val configRoles: List<AdRolle> = config.roles
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

private fun AuthenticationConfig.jwtSystemToken(config: Configuration.TokenVerificationConfig, name: String, realm: String, roles: List<Rolle>? = null) =
    jwt(name) {
        SECURELOG.info { "config : $config" }
        this.realm = realm
        val jwkProviderGammel = UrlJwkProvider(URI(config.jwksUri).toURL())
        verifier(jwkProviderGammel, config.issuer) {
            LOG.info { "Er nå i verifier" }
            withAudience(config.clientId)
            acceptLeeway(config.leeway)
        }
        challenge { _, _ ->
            LOG.info { "verifier feilet" }
            call.respond(HttpStatusCode.Unauthorized, "Ikke tilgang")
        }
        validate { cred ->
            SECURELOG.info("Cred er $cred")
            LOG.info { "er nå i validate, skal ha oid" }
            if (cred.getClaim("oid", String::class) == null) {
                LOG.info { "Fant ikke oid" }
                return@validate null
            }
            LOG.info { "er nå i validate, skal ha sub" }
            if (cred.getClaim("sub", String::class) == null) {
                LOG.info { "Fant ikke sub" }
                return@validate null
            }
            LOG.info { "er nå i validate, skal ha azp_name" }
            if (cred.getClaim("azp_name", String::class) == null) {
                LOG.info { "Fant ikke azp_name" }
                return@validate null
            }

            if (cred.getClaim("oid", String::class) != cred.getClaim("sub", String::class)) {
                LOG.info { "oid og sub er ikke like" }
                return@validate null
            }

            val claimedRoles: List<String> = cred.getListClaim("roles", String::class).map { it.uppercase() }
            LOG.info { "Vi fant disse rollene i token : $claimedRoles" }
            val authorizedRoles = roles?.map { it.name }
            LOG.info { "Dette er gyldige roller : $authorizedRoles" }

            if (!authorizedRoles.isNullOrEmpty()) {
                if (claimedRoles.none(authorizedRoles::contains)) {
                    LOG.info { "Fant ikke riktig rolle" }
                    return@validate null
                }
            }

            JWTPrincipal(cred.payload)
        }
    }

fun Application.auth(config: Configuration.TokenVerificationConfig) {

    /*
    val jwkProvider: JwkProvider = JwkProviderBuilder(config.jwksUri)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
     */

    install(Authentication) {
        jwt(config, "saksbehandling", "saksbehandling", listOf(Rolle.SAKSBEHANDLER))
        jwtSystemToken(config, "systemtoken", "systemtoken", listOf(Rolle.LAGE_HENDELSER))
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
