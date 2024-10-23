package no.nav.tiltakspenger.vedtak.auth

import arrow.core.Either
import arrow.core.getOrElse
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.vedtak.db.objectMapper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.newBuilder
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class EntraIdSystemtokenHttpClient(
    baseUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    connectTimeout: kotlin.time.Duration = 1.seconds,
    private val timeout: kotlin.time.Duration = 6.seconds,
) : EntraIdSystemtokenClient {

    private val log = KotlinLogging.logger {}

    private val cache: AsyncCache<String, AccessToken> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfter(expireAfter())
        .buildAsync()

    private val client = newBuilder()
        .connectTimeout(connectTimeout.toJavaDuration())
        .followRedirects(Redirect.NEVER)
        .build()

    private val uri: URI = URI.create(baseUrl)

    private fun formData(otherAppId: String): String {
        val urlEncodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8)
        val urlEncodedClientSecret = URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
        val urlEncodedOtherAppId = URLEncoder.encode(otherAppId, StandardCharsets.UTF_8)
        return "grant_type=client_credentials&client_id=$urlEncodedClientId&client_secret=$urlEncodedClientSecret&scope=$urlEncodedOtherAppId"
    }

    override suspend fun getSystemtoken(
        otherAppId: String,
        forceUpdateCache: Boolean,
    ): AccessToken {
        return withContext(Dispatchers.IO) {
            if (forceUpdateCache) {
                invalidateToken(otherAppId)
            }
            cache.get(otherAppId) { _, _ ->
                log.debug("Henter systemtoken for $otherAppId")
                future {
                    generateSystemtoken(otherAppId).also {
                        log.debug("Systemtoken hentet for $otherAppId")
                    }
                }
            }.await()
        }
    }

    override fun invalidateToken(otherAppId: String) {
        cache.synchronous().invalidate(otherAppId)
    }

    private suspend fun generateSystemtoken(
        otherAppId: String,
    ): AccessToken {
        return Either.catch {
            val formData = formData(otherAppId)
            val request = createRequest(formData)
            val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
            val jsonResponse = httpResponse.body()
            val status = httpResponse.statusCode()
            if (status != 200) {
                sikkerlogg.error("Feil ved henting av systemtoken mot $otherAppId. Status: $status. jsonResponse: $jsonResponse. uri: $uri.  formData: $formData")
                throw RuntimeException("Feil ved henting av systemtoken mot $otherAppId. Status: $status. uri: $uri. Se sikkerlogg for detaljer.")
            }
            Either.catch {
                val json = objectMapper.readTree(jsonResponse)
                AccessToken(
                    token = json.get("access_token").toString(),
                    expiresAt = Instant.now().plusSeconds(json.get("expires_in").asLong()),
                    invaliderCache = { invalidateToken(otherAppId) },
                )
            }.getOrElse {
                sikkerlogg.error(it) { "Feil ved parsing av respons fra Entra id client_credentials. status: $status, otherAppId:$otherAppId. jsonResponse: $jsonResponse. uri: $uri" }
                throw RuntimeException("Feil ved parsing av respons fra Entra id client_credentials. status: $status, otherAppId:$otherAppId. Se sikkerlogg for detaljer.")
            }
        }.getOrElse {
            // Either.catch slipper igjennom CancellationException som er ønskelig.
            sikkerlogg.error(it) { "Ukjent feil ved kall mot Azure client_credentials. otherAppId: $otherAppId" }
            throw RuntimeException("Feil ved henting av systemtoken mot $otherAppId.nummer. Se sikkerlogg for detaljer.")
        }
    }

    private fun createRequest(
        formData: String,
    ): HttpRequest? {
        return HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
    }
}

private fun expireAfter(): Expiry<String, AccessToken> {
    return object : Expiry<String, AccessToken> {
        override fun expireAfterCreate(key: String, value: AccessToken, currentTime: Long): Long {
            return value.remainingNanos()
        }

        override fun expireAfterUpdate(
            key: String,
            value: AccessToken,
            currentTime: Long,
            currentDuration: Long,
        ): Long {
            return value.remainingNanos()
        }

        override fun expireAfterRead(
            key: String,
            value: AccessToken,
            currentTime: Long,
            currentDuration: Long,
        ): Long {
            return currentDuration
        }
    }
}
