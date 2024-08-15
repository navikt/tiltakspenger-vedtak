package no.nav.tiltakspenger.saksbehandling.service.meldekort

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortgrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo

/**
 * Har som oppgave og sende vedtak til meldekort.
 * At least once garanti. Her bør mottakeren dedupe på vedtakId.
 */
class MeldekortgrunnlagService(
    private val meldekortGrunnlagGateway: MeldekortgrunnlagGateway,
    private val vedtakRepo: VedtakRepo,
) {

    val log = KotlinLogging.logger { }

    suspend fun sendNyeVedtak(correlationId: CorrelationId) {
        vedtakRepo.hentVedtakSomIkkeErSendtTilMeldekort().forEach { vedtak ->
            Either.catch {
                meldekortGrunnlagGateway.sendMeldekortGrunnlag(vedtak, correlationId)
            }.mapLeft {
                log.error(it) { "Feil ved sending av vedtak ${vedtak.id} til tiltakspenger-meldekort-api" }
            }
        }
    }
}
