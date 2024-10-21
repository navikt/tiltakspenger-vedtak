package no.nav.tiltakspenger.vedtak.clients.person

import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.right
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.NavIdentClient
import no.nav.tiltakspenger.felles.exceptions.IkkeLoggDenneException
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.vedtak.db.deserialize
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private data class MicrosoftGraphResponse(
    val displayName: String,
)

private data class ListOfMicrosoftGraphResponse(
    val value: List<MicrosoftGraphResponse>,
)

class MicrosoftGraphApiClient(
    private val getToken: suspend () -> AccessToken,
    private val timeout: Duration = 1.seconds,
    private val baseUrl: String = "https://graph.microsoft.com/v1.0",
    connectTimeout: Duration = 1.seconds,
) : NavIdentClient {
    private val log = KotlinLogging.logger { }

    private fun uri(navIdent: String) =
        URI.create("$baseUrl/v1.0/users?\$select=displayName&\$filter=onPremisesSamAccountName eq '$navIdent'\$count=true")

    private val client =
        java.net.http.HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout.toJavaDuration())
            .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
            .build()

    /**
     * Denne returnerer navnet til saksbehandler eller kaster runtimeException om noe feiler
     */
    override suspend fun hentNavnForNavIdent(navIdent: String): String {
        return hentBrukerinformasjonForNavIdent(navIdent).let { brukerInfo ->
            val saksbehandlersNavn = brukerInfo.displayName.trim()
            if (saksbehandlersNavn.isBlank()) {
                throw RuntimeException("Fant ikke saksbehandlerens navn i microsoftGraphApi $navIdent. Responsen var blank.")
            }
            // Todo Kew: Er dette formatet "[Etternavn], [Fornavn]"? Isåfall bør man snu det til "[Fornavn] [Etternavn]"
            saksbehandlersNavn
        }
    }

    private suspend fun hentBrukerinformasjonForNavIdent(navIdent: String): MicrosoftGraphResponse {
        return Either.catch {
            val uri = uri(navIdent)
            val request = createRequest(uri)
            val httpResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
            val jsonResponse = httpResponse.body().let { deserialize<ListOfMicrosoftGraphResponse>(it) }
            jsonResponse.let { response ->
                if (response.value.size != 1) {
                    log.error("Fant ingen eller flere brukere for navIdent $navIdent: ${response.value.size}. Se sikker logg dersom vi fant flere.")
                    if (response.value.isNotEmpty()) {
                        sikkerlogg.error("Fant ingen eller flere brukere for navIdent $navIdent: ${response.value}")
                    }
                    throw RuntimeException("Fant ikke bruker for navident: $navIdent")
                } else {
                    response.value.first().right()
                }
            }
        }.flatten().getOrElse {
            log.error(RuntimeException("Genererer stacktrace for enklere debug")) { "Ukjent feil mot Microsoft Graph Api for bruker $navIdent. Se sikker logg for mer context" }
            sikkerlogg.error(it) { "Ukjent feil mot Microsoft Graph Api for bruker $navIdent: ${it.message}" }
            throw IkkeLoggDenneException("Denne er logget alt, trenger ikke spamme loggen mer enn nødvendig")
        }
    }

    private suspend fun createRequest(
        uri: URI,
    ): HttpRequest {
        return HttpRequest
            .newBuilder()
            .uri(uri)
            .timeout(timeout.toJavaDuration())
            .header("Accept", "application/json")
            .header("Authorization", "Bearer ${getToken().value}")
            .header("ConsistencyLevel", "eventual")
            .GET()
            .build()
    }
}
