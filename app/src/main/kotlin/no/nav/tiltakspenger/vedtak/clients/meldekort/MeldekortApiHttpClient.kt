package no.nav.tiltakspenger.vedtak.clients.meldekort

import arrow.core.Either
import arrow.core.left
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.FeilVedSendingTilMeldekortApi
import no.nav.tiltakspenger.meldekort.ports.MeldekortApiHttpClientGateway
import no.nav.tiltakspenger.vedtak.clients.defaultHttpClient

class MeldekortApiHttpClient(
    baseUrl: String,
    private val getToken: suspend () -> AccessToken,
) : MeldekortApiHttpClientGateway {
    private val client = defaultHttpClient()
    private val logger = KotlinLogging.logger {}
    private val url = "$baseUrl/meldekort"

    override suspend fun sendMeldekort(meldekort: Meldekort): Either<FeilVedSendingTilMeldekortApi, Unit> {
        return Either.catch {
            val response = client.preparePost(url) {
                bearerAuth(getToken().token)
                contentType(ContentType.Application.Json)
                setBody(meldekort.tilBrukerDTO())
            }.execute()

            if (response.status != HttpStatusCode.OK) {
                val body: String = response.call.body()
                "Feilrespons ved sending av ${meldekort.id} til meldekort-api - status: ${response.status}".let { msg ->
                    logger.error(msg)
                    sikkerlogg.error { "$msg - Response body: $body" }
                }
                return FeilVedSendingTilMeldekortApi.left()
            }
        }.mapLeft {
            "Feil ved sending av ${meldekort.id} til meldekort-api".let { msg ->
                logger.error { msg }
                sikkerlogg.error(it) { msg }
            }
            FeilVedSendingTilMeldekortApi
        }
    }
}
