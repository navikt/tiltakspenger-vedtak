package no.nav.tiltakspenger.utbetaling.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

/**
 * Har ansvar for å sende klare utbetalingsvedtak til helved utsjekk.
 */
class SendUtbetalingerService(
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val utbetalingsklient: UtbetalingGateway,
) {
    val logger = KotlinLogging.logger { }
    suspend fun send(correlationId: CorrelationId) {
        Either.catch {
            utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk().forEach { utbetalingsvedtak ->
                Either.catch {
                    val forrigeUtbetalingJson =
                        utbetalingsvedtak.forrigeUtbetalingsvedtakId?.let {
                            utbetalingsvedtakRepo.hentUtbetalingJsonForVedtakId(
                                it,
                            )
                        }
                    utbetalingsklient.iverksett(utbetalingsvedtak, forrigeUtbetalingJson, correlationId).onRight {
                        logger.info { "Utbetaling iverksatt for vedtak ${utbetalingsvedtak.id}" }
                        utbetalingsvedtakRepo.markerSendtTilUtbetaling(utbetalingsvedtak.id, nå(), it)
                        logger.info { "Utbetaling markert som utbetalt for vedtak ${utbetalingsvedtak.id}" }
                    }.onLeft {
                        logger.error { "Utbetaling kunne ikke iverksettes. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
                    }
                }.onLeft {
                    logger.error(it) { "Ukjent feil skjedde under iverksetting av utbetaling. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
                }
            }
        }.onLeft {
            logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ukjent feil skjedde under utbetaling mot helved" }
            sikkerlogg.error(it) { "Ukjent feil skjedde under utbetaling mot helved" }
        }
    }
}
