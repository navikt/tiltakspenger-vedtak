package no.nav.tiltakspenger.utbetaling.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.meldekort.ports.JournalførMeldekortGateway
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import java.time.LocalDateTime

/**
 * Har ansvar for å generere pdf og sende utbetalingsvedtak til journalføring.
 * Denne er kun ment og kalles fra en jobb.
 */
class JournalførUtbetalingsvedtakService(
    private val journalførMeldekortGateway: JournalførMeldekortGateway,
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val genererMeldekortPdfGateway: GenererMeldekortPdfGateway,
    private val personService: PersonService,
) {
    private val log = KotlinLogging.logger { }

    suspend fun journalfør(correlationId: CorrelationId) {
        utbetalingsvedtakRepo.hentDeSomSkalJournalføres().forEach { utbetalingsvedtak ->
            log.info { "Journalfører utbetalingsvedtak. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
            Either.catch {
                val pdfOgJson =
                    genererMeldekortPdfGateway.genererMeldekortPdf(utbetalingsvedtak.meldekort, personService::hentNavn)
                log.info { "Pdf generert for utbetalingsvedtak. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
                val journalpostId = journalførMeldekortGateway.journalførMeldekort(
                    meldekort = utbetalingsvedtak.meldekort,
                    pdfOgJson = pdfOgJson,
                    correlationId = correlationId,
                )
                log.info { "utbetalingsvedtak journalført. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}. JournalpostId: $journalpostId" }
                utbetalingsvedtakRepo.markerJournalført(utbetalingsvedtak.id, journalpostId, LocalDateTime.now())
                log.info { "Utbetalingsvedtak markert som journalført. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}. JournalpostId: $journalpostId" }
            }.onLeft {
                log.error(it) { "Ukjent feil skjedde under generering av brev og journalføring av utbetalingsvedtak. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
            }
        }
    }
}
