package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.common.JournalpostIdGenerator
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.meldekort.ports.JoarkResponse
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import java.time.LocalDateTime

class DokumentGatewayFake(
    private val journalpostIdGenerator: JournalpostIdGenerator,
) : DokumentGateway {

    private val brevSendt: Atomic<Map<SakId, BrevSendt>> = Atomic(mutableMapOf())
    val antallBrevSendt: Int get() = brevSendt.get().size

    override suspend fun sendMeldekortTilDokument(
        vedtak: Utbetalingsvedtak,
        correlationId: CorrelationId,
    ): JoarkResponse {
        val response = JoarkResponse(journalpostIdGenerator.neste(), LocalDateTime.now())
        brevSendt.get().plus(
            vedtak.sakId to BrevSendt(
                vedtak,
                correlationId,
                response,
            ),
        )
        return response
    }

    fun hentBrevSendt(sakId: SakId): BrevSendt? {
        return brevSendt.get()[sakId]
    }

    /** Hent på sakId er raskere. */
    fun hentBrevSendt(saksnummer: Saksnummer): BrevSendt? {
        return brevSendt.get().values.find { it.vedtak.saksnummer == saksnummer }
    }

    data class BrevSendt(
        val vedtak: Utbetalingsvedtak,
        val correlationId: CorrelationId,
        val response: JoarkResponse,
    )
}
