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
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.innsending.service.InnsendingAdminService
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerService
import no.nav.tiltakspenger.vedtak.AdRolle
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.routes.admin.resettInnsendingerRoute
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingBenkRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingBeslutterRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.behandlingRoutes
import no.nav.tiltakspenger.vedtak.routes.behandling.saksopplysningRoutes
import no.nav.tiltakspenger.vedtak.routes.exceptionhandling.ExceptionHandler
import no.nav.tiltakspenger.vedtak.routes.meldekort.meldekortRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.foreldrepengerRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.innsendingUtdatertRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.overgangsstønadRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.passageOfTimeRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.personopplysningerRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.skjermingRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.søknad.søknadRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.tiltakRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.uføreRoutes
import no.nav.tiltakspenger.vedtak.routes.rivers.ytelseRoutes
import no.nav.tiltakspenger.vedtak.routes.sak.sakRoutes
import no.nav.tiltakspenger.vedtak.routes.saksbehandler.saksbehandlerRoutes
import no.nav.tiltakspenger.vedtak.routes.søker.søkerRoutes
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSystembrukerProvider
import java.net.URI
import java.util.UUID

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal fun Application.vedtakApi(
    config: Configuration.TokenVerificationConfig,
    innloggetSaksbehandlerProvider: JWTInnloggetSaksbehandlerProvider,
    innloggetSystembrukerProvider: JWTInnloggetSystembrukerProvider,
    søkerService: SøkerService,
    sakService: SakService,
    behandlingService: BehandlingService,
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
    innsendingAdminService: InnsendingAdminService,
    attesteringRepo: AttesteringRepo,
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
            behandlingRoutes(
                innloggetSaksbehandlerProvider = innloggetSaksbehandlerProvider,
                behandlingService = behandlingService,
                sakService = sakService,
                innsendingMediator = innsendingMediator,
                attesteringRepo = attesteringRepo,
            )
            behandlingBenkRoutes(
                innloggetSaksbehandlerProvider = innloggetSaksbehandlerProvider,
                behandlingService = behandlingService,
                søkerService = søkerService,
            )
            behandlingBeslutterRoutes(
                innloggetSaksbehandlerProvider = innloggetSaksbehandlerProvider,
                behandlingService = behandlingService,
            )
            sakRoutes(
                innloggetSaksbehandlerProvider = innloggetSaksbehandlerProvider,
                søkerService = søkerService,
                sakService = sakService,
            )
            meldekortRoutes()
            saksopplysningRoutes()
        }
        authenticate("admin") {
            resettInnsendingerRoute(innsendingAdminService)
        }
        authenticate("systemtoken") {
            søknadRoutes(innsendingMediator, søkerMediator, sakService)
            skjermingRoutes(innsendingMediator, sakService)
            tiltakRoutes(innsendingMediator, behandlingService)
            ytelseRoutes(innsendingMediator, behandlingService)
            foreldrepengerRoutes(innsendingMediator, behandlingService)
            overgangsstønadRoutes(innsendingMediator)
            uføreRoutes(innsendingMediator, behandlingService)
            personopplysningerRoutes(
                innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                innsendingMediator = innsendingMediator,
                søkerMediator = søkerMediator,
                sakService = sakService,
            )
            passageOfTimeRoutes(
                innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                sakService = sakService,
            )
            innsendingUtdatertRoutes(
                innloggetSystembrukerProvider = innloggetSystembrukerProvider,
                innsendingMediator = innsendingMediator,
            )
        }
        staticResources(
            remotePath = "/",
            basePackage = "static",
            index = "index.html",
            block = {
                default("index.html")
            },
        )
    }
}

private fun AuthenticationConfig.jwt(
    config: Configuration.TokenVerificationConfig,
    name: String,
    realm: String,
    roles: List<Rolle>? = null,
) =
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

private fun AuthenticationConfig.jwtSystemToken(
    config: Configuration.TokenVerificationConfig,
    name: String,
    realm: String,
    roles: List<Rolle>? = null,
) =
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
    install(Authentication) {
        jwt(
            config,
            "saksbehandling",
            "saksbehandling",
            listOf(Rolle.SAKSBEHANDLER, Rolle.BESLUTTER, Rolle.ADMINISTRATOR),
        )
        jwt(config, "admin", "saksbehandling", listOf(Rolle.DRIFT))
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

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            ExceptionHandler.handle(call, cause)
        }
    }
}
