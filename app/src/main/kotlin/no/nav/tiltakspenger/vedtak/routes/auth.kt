package no.nav.tiltakspenger.vedtak.routes

import com.papsign.ktor.openapigen.model.Described
import com.papsign.ktor.openapigen.model.security.HttpSecurityScheme
import com.papsign.ktor.openapigen.model.security.SecuritySchemeModel
import com.papsign.ktor.openapigen.model.security.SecuritySchemeType
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.util.pipeline.PipelineContext

/* This is used to document auth and provide a way to authenticate requests
* inside a NormalOpenAPIRoute
* */

inline fun NormalOpenAPIRoute.auth(route: OpenAPIAuthenticatedRoute<JWTPrincipal>.() -> Unit): OpenAPIAuthenticatedRoute<JWTPrincipal> {
    val authenticatedKtorRoute = this.ktorRoute.authenticate { }
    val openAPIAuthenticatedRoute = OpenAPIAuthenticatedRoute(
        authenticatedKtorRoute,
        this.provider.child(),
        authProvider = JwtProvider()
    )
    return openAPIAuthenticatedRoute.apply {
        route()
    }
}

enum class Scopes(override val description: String) : Described {
    Profile("Some scope")
}

class JwtProvider : AuthProvider<JWTPrincipal> {
    override val security: Iterable<Iterable<AuthProvider.Security<*>>> =
        listOf(listOf(
            AuthProvider.Security(
                SecuritySchemeModel(
                    SecuritySchemeType.http,
                    scheme = HttpSecurityScheme.bearer,
                    bearerFormat = "JWT",
                    referenceName = "jwtAuth",
                ), emptyList<Scopes>()
            )
        ))

    override suspend fun getAuth(pipeline: PipelineContext<Unit, ApplicationCall>): JWTPrincipal {
        return pipeline.context.authentication.principal() ?: throw RuntimeException("No JWTPrincipal")
    }

    override fun apply(route: NormalOpenAPIRoute): OpenAPIAuthenticatedRoute<JWTPrincipal> {
        val authenticatedKtorRoute = route.ktorRoute.authenticate { }
        return OpenAPIAuthenticatedRoute(authenticatedKtorRoute, route.provider.child(), this)
    }
}
