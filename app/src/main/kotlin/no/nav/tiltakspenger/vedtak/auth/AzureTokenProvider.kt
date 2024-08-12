package no.nav.tiltakspenger.vedtak.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper

private val LOG = KotlinLogging.logger {}

fun interface TokenProvider {
    suspend fun getToken(): AccessToken
}

class AzureTokenProvider(
    objectMapper: ObjectMapper = defaultObjectMapper(),
    engine: HttpClientEngine? = null,
    private val config: OauthConfig,
) : TokenProvider {
    private val azureHttpClient =
        defaultHttpClient(objectMapper = objectMapper, engine = engine) {
            System.getenv("HTTP_PROXY")?.let {
                LOG.info("Setter opp proxy mot $it")
                this.proxy = ProxyBuilder.http(it)
            }
        }

    private val tokenCache = TokenCache()

    override suspend fun getToken(): AccessToken {
        try {
            val currentToken: AccessToken? = tokenCache.token
            return if (currentToken != null && !tokenCache.isExpired()) {
                currentToken
            } else {
                clientCredentials()
            }
        } catch (e: Exception) {
            throw AzureAuthException(e)
        }
    }

    private suspend fun wellknown(): WellKnown = azureHttpClient.get(config.wellknownUrl).body()

    private suspend fun clientCredentials(): AccessToken {
        return azureHttpClient
            .submitForm(
                url = wellknown().tokenEndpoint,
                formParameters =
                Parameters.build {
                    append("grant_type", "client_credentials")
                    append("client_id", config.clientId)
                    append("client_secret", config.clientSecret)
                    append("scope", config.scope)
                },
            ).body<OAuth2AccessTokenResponse>()
            .let {
                val accessToken = AccessToken(it.accessToken)
                tokenCache.update(
                    accessToken = accessToken,
                    expiresIn = it.expiresIn.toLong(),
                )
                return@let accessToken
            }
    }

    data class OauthConfig(
        val scope: String,
        val clientId: String,
        val clientSecret: String,
        val wellknownUrl: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WellKnown(
        @JsonProperty("token_endpoint")
        val tokenEndpoint: String,
    )

    class AzureAuthException(
        e: Exception,
    ) : RuntimeException(e)
}
