package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.meldekort.ports.MeldekortApiHttpClientGateway
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

/**
 * Sender meldekort som er klare for utfylling til meldekort-api, som serverer videre til bruker
 */
class SendMeldekortTilBrukerService(
    private val meldekortRepo: MeldekortRepo,
    private val meldekortApiHttpClient: MeldekortApiHttpClientGateway,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun send() {
        Either.catch {
            val meldekortTilUtfylling = meldekortRepo.hentTilBrukerUtfylling()

            logger.info("Fant ${meldekortTilUtfylling.count()} meldekort for sending til meldekort-api")

            meldekortTilUtfylling.forEach { meldekort ->
                meldekortApiHttpClient.sendMeldekort(meldekort).onRight {
                    logger.debug { "Sendte meldekort til utfylling med id ${meldekort.id}" }
//                meldekortRepo.markerSomSendtTilBrukerUtfylling(meldekort.id, nå())
                }.onLeft {
                    logger.error { "Kunne ikke sende meldekort til utfylling med id ${meldekort.id}" }
                }
            }
        }.onLeft {
            logger.error(RuntimeException("Oh noes")) { "Feil ved sending av meldekort til meldekort-api! ${it.message}" }
        }
    }
}
