package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

/**
 * Har ansvar for å sende utbetalingsvedtak/meldekort til tiltakspenger-dokument.
 * Denne er kun ment og kalles fra en jobb.
 */
class JournalførMeldekortService(
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val dokumentGateway: DokumentGateway,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun send(correlationId: CorrelationId) {
        utbetalingsvedtakRepo.hentUtbetalingsvedtakForDokument().forEach { utbetalingsvedtak ->
            Either.catch {
                // TODO pre-mvp jah: Diskuter med teamet om vi trenger en egen pod for å journalføre? Den er jo uansett synkron så vedtak vil ta seg av retries. Kunne det vært et lib istedenfor?
                val response = dokumentGateway.sendMeldekortTilDokument(utbetalingsvedtak, correlationId)
                logger.info { "Utbetalingsvedtak journalført. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}. Response: $response" }
                utbetalingsvedtakRepo.markerSendtTilDokument(utbetalingsvedtak.id)
                logger.info { "Utbetalingsvedtak markert som sendt til dokument. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}. Response: $response" }
            }.onLeft {
                logger.error(it) { "Ukjent feil skjedde under journalføring av utbetaling. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
            }
        }
    }
}
