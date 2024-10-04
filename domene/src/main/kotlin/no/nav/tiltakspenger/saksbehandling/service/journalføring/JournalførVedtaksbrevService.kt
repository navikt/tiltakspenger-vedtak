package no.nav.tiltakspenger.saksbehandling.service.journalføring

import arrow.core.Either
import arrow.core.getOrElse
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonService
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.JournalførVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import java.time.LocalDateTime

class JournalførVedtaksbrevService(
    private val journalførVedtaksbrevGateway: JournalførVedtaksbrevGateway,
    private val rammevedtakRepo: RammevedtakRepo,
    private val genererVedtaksbrevGateway: GenererVedtaksbrevGateway,
    private val personService: PersonService,
) {
    private val log = KotlinLogging.logger {}

    /** Ment å kalles fra en jobb - journalfører alle rammevedtak som skal sende brev. */
    suspend fun journalfør(
        correlationId: CorrelationId,
    ) {
        rammevedtakRepo.hentRammevedtakSomSkalJournalføres().forEach { vedtak ->
            log.info { "Journalfører vedtaksbrev for vedtak ${vedtak.id}" }
            Either.catch {
                val pdfOgJson = genererVedtaksbrevGateway.genererVedtaksbrev(vedtak, personService::hentNavn).getOrElse { return@forEach }
                log.info { "Vedtaksbrev generert for vedtak ${vedtak.id}" }
                val journalpostId = journalførVedtaksbrevGateway.journalførVedtaksbrev(vedtak, pdfOgJson, correlationId)
                log.info { "Vedtaksbrev journalført for vedtak ${vedtak.id}" }
                rammevedtakRepo.markerJournalført(vedtak.id, journalpostId, LocalDateTime.now())
                log.info { "Vedtaksbrev markert som journalført for vedtak ${vedtak.id}" }
            }.onLeft {
                log.error(it) { "Feil ved journalføring av vedtaksbrev for vedtak ${vedtak.id}" }
            }
        }
    }
}
