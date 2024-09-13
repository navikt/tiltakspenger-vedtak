package no.nav.tiltakspenger.saksbehandling.service.distribuering

import arrow.core.Either
import arrow.core.getOrElse
import mu.KotlinLogging
import no.nav.tiltakspenger.distribusjon.ports.DokdistGateway
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import java.time.LocalDateTime

class DistribuerVedtaksbrevService(
    private val dokdistGateway: DokdistGateway,
    private val rammevedtakRepo: RammevedtakRepo,
) {
    private val log = KotlinLogging.logger {}

    /** Ment å kalles fra en jobb - journalfører alle rammevedtak som skal sende brev. */
    suspend fun distribuer(
        correlationId: CorrelationId,
    ) {
        rammevedtakRepo.hentRammevedtakSomSkalDistribueres().forEach { vedtakSomSkalDistribueres ->
            log.info { "Prøver å distribuere journalpost  for rammevedtak. $vedtakSomSkalDistribueres" }
            Either.catch {
                val distribusjonId =
                    dokdistGateway.distribuerDokument(vedtakSomSkalDistribueres.journalpostId, correlationId)
                        .getOrElse {
                            log.error { "Kunne ikke distribuere vedtaksbrev. $vedtakSomSkalDistribueres" }
                            return@forEach
                        }
                log.info { "Vedtaksbrev distribuert. $vedtakSomSkalDistribueres" }
                rammevedtakRepo.markerDistribuert(vedtakSomSkalDistribueres.id, distribusjonId, LocalDateTime.now())
                log.info { "Vedtaksbrev markert som distribuert. distribusjonId: $distribusjonId, $vedtakSomSkalDistribueres" }
            }.onLeft {
                log.error(it) { "Feil ved journalføring av vedtaksbrev. $vedtakSomSkalDistribueres" }
            }
        }
    }
}
