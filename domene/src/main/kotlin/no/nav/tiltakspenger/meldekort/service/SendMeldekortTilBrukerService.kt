package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.nå
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
            val meldekortTilBruker = meldekortRepo.hentUsendteTilBruker()

            logger.debug("Fant ${meldekortTilBruker.count()} meldekort for sending til meldekort-api")

            meldekortTilBruker.forEach { meldekort ->
                meldekortApiHttpClient.sendMeldekort(meldekort).onRight {
                    logger.info { "Sendte meldekort til meldekort-api med id ${meldekort.id}" }
                    meldekortRepo.markerSomSendtTilBruker(meldekort.id, nå())
                }.onLeft {
                    logger.error { "Kunne ikke sende meldekort til meldekort-api med id ${meldekort.id}" }
                }
            }
        }.onLeft {
            with("Uventet feil ved sending av meldekort til meldekort-api!") {
                logger.error(RuntimeException("Uventet feil!")) { this }
                sikkerlogg.error(it) { this }
            }
        }
    }
}
