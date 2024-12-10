package no.nav.tiltakspenger.vedtak.clients.meldekort

import arrow.core.Either
import arrow.core.left
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.tiltakspenger.libs.auth.core.EntraIdSystemtokenClient
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.FeilVedSendingTilMeldekortApi
import no.nav.tiltakspenger.meldekort.ports.MeldekortApiHttpClientGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient

class MeldekortApiHttpClient(
    private val entraIdSystemtokenClient: EntraIdSystemtokenClient,
) : MeldekortApiHttpClientGateway {
    private val client = defaultHttpClient()

    private val baseUrl = Configuration.meldekortApiUrl
    private val scope = Configuration.meldekortApiScope

    override suspend fun sendMeldekort(meldekort: Meldekort): Either<FeilVedSendingTilMeldekortApi, Unit> {
        return Either.catch {
            val body = meldekort.tilUtfyllingDTO()
            val token = entraIdSystemtokenClient.getSystemtoken(scope)
            val response = client.preparePost("$baseUrl/saksbehandling/meldekort") {
                bearerAuth(token.token)
                contentType(ContentType.Application.Json)
                setBody(body)
            }.execute()

            if (response.status != HttpStatusCode.OK) {
                return FeilVedSendingTilMeldekortApi.left()
            }
        }.mapLeft {
            FeilVedSendingTilMeldekortApi
        }
    }
}
