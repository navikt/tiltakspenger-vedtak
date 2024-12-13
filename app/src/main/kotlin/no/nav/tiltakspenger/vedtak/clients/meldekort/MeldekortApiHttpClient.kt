package no.nav.tiltakspenger.vedtak.clients.meldekort

import arrow.core.Either
import arrow.core.left
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.FeilVedSendingTilMeldekortApi
import no.nav.tiltakspenger.meldekort.ports.MeldekortApiHttpClientGateway
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient

class MeldekortApiHttpClient(
    private val baseUrl: String,
    private val getToken: suspend () -> AccessToken,
) : MeldekortApiHttpClientGateway {
    private val client = defaultHttpClient()

    override suspend fun sendMeldekort(meldekort: Meldekort): Either<FeilVedSendingTilMeldekortApi, Unit> {
        return Either.catch {
            val body = meldekort.tilBrukerDTO()
            val token = getToken()
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
