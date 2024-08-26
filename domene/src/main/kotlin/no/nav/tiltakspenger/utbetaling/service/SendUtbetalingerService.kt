package no.nav.tiltakspenger.utbetaling.service

import arrow.core.Either
import mu.KotlinLogging
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
    suspend fun send(correlationId: CorrelationId) {
        val logger = KotlinLogging.logger { }

        utbetalingsvedtakRepo.hentUtbetalingsvedtakForUtsjekk().forEach { utbetalingsvedtak ->
            Either.catch {
                utbetalingsklient.iverksett(utbetalingsvedtak, correlationId).onRight {
                    logger.info { "Utbetaling iverksatt for vedtak ${utbetalingsvedtak.id}" }
                    utbetalingsvedtakRepo.markerUtbetalt(utbetalingsvedtak.id, it)
                    logger.info { "Utbetaling markert som utbetalt for vedtak ${utbetalingsvedtak.id}" }
                }.onLeft {
                    logger.error { "Utbetaling kunne ikke iverksettes. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
                }
            }.onLeft {
                logger.error(it) { "Ukjent feil skjedde under iverksetting av utbetaling. Saksnummer: ${utbetalingsvedtak.saksnummer}, sakId: ${utbetalingsvedtak.sakId}, utbetalingsvedtakId: ${utbetalingsvedtak.id}" }
            }
        }
    }
}
