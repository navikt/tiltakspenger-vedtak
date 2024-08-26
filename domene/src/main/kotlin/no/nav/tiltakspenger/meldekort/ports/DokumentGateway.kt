package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import java.time.LocalDateTime

interface DokumentGateway {
    suspend fun sendMeldekortTilDokument(
        vedtak: Utbetalingsvedtak,
        correlationId: CorrelationId,
    ): JoarkResponse
}

data class JoarkResponse(
    val journalpostId: String,
    val innsendingTidspunkt: LocalDateTime,
)
