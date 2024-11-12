package no.nav.tiltakspenger.saksbehandling.service.journalføring

import arrow.core.Either
import arrow.core.getOrElse
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.NavIdentClient
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.JournalførVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService

class JournalførVedtaksbrevService(
    private val journalførVedtaksbrevGateway: JournalførVedtaksbrevGateway,
    private val rammevedtakRepo: RammevedtakRepo,
    private val genererVedtaksbrevGateway: GenererVedtaksbrevGateway,
    private val personService: PersonService,
    private val navIdentClient: NavIdentClient,
) {
    private val log = KotlinLogging.logger {}

    /** Ment å kalles fra en jobb - journalfører alle rammevedtak som skal sende brev. */
    suspend fun journalfør(
        correlationId: CorrelationId,
    ) {
        Either.catch {
            rammevedtakRepo.hentRammevedtakSomSkalJournalføres().forEach { vedtak ->
                log.info { "Journalfører vedtaksbrev for vedtak ${vedtak.id}" }
                Either.catch {
                    val pdfOgJson = genererVedtaksbrevGateway.genererVedtaksbrev(
                        vedtak = vedtak,
                        hentBrukersNavn = personService::hentNavn,
                        hentSaksbehandlersNavn = navIdentClient::hentNavnForNavIdent,
                    ).getOrElse { return@forEach }
                    log.info { "Vedtaksbrev generert for vedtak ${vedtak.id}" }
                    val journalpostId =
                        journalførVedtaksbrevGateway.journalførVedtaksbrev(vedtak, pdfOgJson, correlationId)
                    log.info { "Vedtaksbrev journalført for vedtak ${vedtak.id}" }
                    rammevedtakRepo.markerJournalført(vedtak.id, journalpostId, nå())
                    log.info { "Vedtaksbrev markert som journalført for vedtak ${vedtak.id}" }
                }.onLeft {
                    log.error(it) { "Feil ved journalføring av vedtaksbrev for vedtak ${vedtak.id}" }
                }
            }
        }.onLeft {
            log.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ukjent feil skjedde under journalføring av førstegangsvedtak." }
            sikkerlogg.error(it) { "Ukjent feil skjedde under journalføring av førstegangsvedtak." }
        }
    }
}
