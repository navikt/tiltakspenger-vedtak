package no.nav.tiltakspenger.vedtak.clients.norg

import arrow.core.Either
import arrow.core.flatten
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.GeografiskOmråde
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.utbetaling.ports.KunneIkkeHenteNavkontor
import no.nav.tiltakspenger.utbetaling.ports.NavkontorGateway
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal class NorgHttpClient(
    private val baseUrl: String,
    private val getToken: suspend () -> AccessToken,
    connectTimeout: Duration = 1.seconds,
    private val timeout: Duration = 1.seconds,
) : NavkontorGateway {

    private val log = KotlinLogging.logger { }

    private val client =
        HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()

    private fun uri(geografiskOmråde: GeografiskOmråde) =
        URI.create("$baseUrl/api/v1/enhet/navkontor/${geografiskOmråde.verdi}")

    override suspend fun hentNavkontor(
        geografiskOmråde: GeografiskOmråde,
    ): Either<KunneIkkeHenteNavkontor, Navkontor> {
        return withContext(Dispatchers.IO) {
            Either.catch {
                val uri = uri(geografiskOmråde)
                val request = createRequest(uri)

                val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
                val jsonResponse = httpResponse.body()
                val httpStatus = httpResponse.statusCode()
                jsonResponse.toNavkontor(geografiskOmråde, httpStatus)
            }.mapLeft {
                // Either.catch slipper igjennom CancellationException som er ønskelig.
                log.error(it) { "Ukjent feil ved henting av navkontor via norg. Geografisk område: ${geografiskOmråde.verdi}." }
                KunneIkkeHenteNavkontor
            }.flatten()
        }
    }

    private suspend fun createRequest(
        uri: URI,
    ): HttpRequest {
        return HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Authorization", "Bearer ${getToken().value}")
            .header("Accept", "application/json")
            .GET()
            .build()
    }
}
