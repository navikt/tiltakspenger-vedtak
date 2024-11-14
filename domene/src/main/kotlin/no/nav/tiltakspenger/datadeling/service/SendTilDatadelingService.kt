package no.nav.tiltakspenger.datadeling.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.datadeling.ports.DatadelingGateway
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import java.time.LocalDateTime

class SendTilDatadelingService(
    private val rammevedtakRepo: RammevedtakRepo,
    private val behandlingRepo: BehandlingRepo,
    private val datadelingGateway: DatadelingGateway,
) {
    val logger = KotlinLogging.logger { }

    suspend fun send(
        correlationId: CorrelationId,
        erNais: Boolean,
        erDev: Boolean,
    ) {
        // Vi dropper og sende disse lokalt. Legg på senere dersom det er behov for det.
        if (erDev) {
            // TODO pre-mvp jah: Enable dette når vi har ferdig klient mot datadeling og routes i datadeling.
            sendBehandlinger(correlationId)
            // sendVedtak(correlationId)
        }
    }

    private suspend fun sendVedtak(correlationId: CorrelationId) {
        Either.catch {
            rammevedtakRepo.hentRammevedtakTilDatadeling().forEach { rammevedtak ->
                Either.catch {
                    datadelingGateway.send(rammevedtak, correlationId).onRight {
                        logger.info { "Vedtak sendt til datadeling. VedtakId: ${rammevedtak.id}" }
                        rammevedtakRepo.markerSendtTilDatadeling(rammevedtak.id, LocalDateTime.now())
                        logger.info { "Vedtak markert som sendt til datadeling. VedtakId: ${rammevedtak.id}" }
                    }.onLeft {
                        logger.error { "Vedtak kunne ikke sendes til datadeling. Saksnummer: ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}, vedtakId: ${rammevedtak.id}" }
                    }
                }.onLeft {
                    logger.error(it) { "Ukjent feil skjedde under sending av vedtak til datadeling. Saksnummer: ${rammevedtak.saksnummer}, sakId: ${rammevedtak.sakId}, vedtakId: ${rammevedtak.id}" }
                }
            }
        }.onLeft {
            logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ukjent feil skjedde under henting av vedtak som skal sendes til datadeling." }
            sikkerlogg.error(it) { "Ukjent feil skjedde under henting av vedtak som skal sendes til datadeling." }
        }
    }

    private suspend fun sendBehandlinger(correlationId: CorrelationId) {
        Either.catch {
            behandlingRepo.hentBehandlingerTilDatadeling().forEach { behandling ->
                Either.catch {
                    datadelingGateway.send(behandling, correlationId).onRight {
                        logger.info { "Behandling sendt til datadeling. Saksnummer: ${behandling.saksnummer}, sakId: ${behandling.sakId}, behandlingId: ${behandling.id}" }
                        behandlingRepo.markerSendtTilDatadeling(behandling.id, LocalDateTime.now())
                        logger.info { "Behandling markert som sendt til datadeling. Saksnummer: ${behandling.saksnummer}, sakId: ${behandling.sakId}, behandlingId: ${behandling.id}" }
                    }.onLeft {
                        logger.error { "Behandling kunne ikke sendes til datadeling. Saksnummer: ${behandling.saksnummer}, sakId: ${behandling.sakId}, behandlingId: ${behandling.id}" }
                    }
                }.onLeft {
                    logger.error(it) { "Ukjent feil skjedde under sending av behandling til datadeling. Saksnummer: ${behandling.saksnummer}, sakId: ${behandling.sakId}, behandlingId: ${behandling.id}" }
                }
            }
        }.onLeft {
            logger.error(RuntimeException("Trigger stacktrace for enklere debug.")) { "Ukjent feil skjedde under henting av behandling som skal sendes til datadeling." }
            sikkerlogg.error(it) { "Ukjent feil skjedde under henting av behandling som skal sendes til datadeling." }
        }
    }
}
