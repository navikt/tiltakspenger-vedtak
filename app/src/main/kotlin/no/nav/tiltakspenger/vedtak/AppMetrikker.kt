package no.nav.tiltakspenger.vedtak

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics
import no.nav.tiltakspenger.innsending.service.ports.InnsendingRepository

object AppMetrikker {

    fun antallInnsendingerLagret(innsendingRepository: InnsendingRepository): Gauge = Gauge
        .builder("antall_innsendinger_lagret") { innsendingRepository.antall() }
        .description("gauge for å telle antall innsendinger lagret i db")
        .register(Metrics.globalRegistry)

    fun antallInnsendingerFeilet(innsendingRepository: InnsendingRepository): Gauge = Gauge
        .builder("antall_innsendinger_tilstand_feilet") { innsendingRepository.antallMedTilstandFaktainnhentingFeilet() }
        .description("gauge for å telle antall innsendinger i db som er i tilstanden FaktainnhentingFeilet")
        .register(Metrics.globalRegistry)

    fun antallInnsendingerStoppetUnderBehandling(innsendingRepository: InnsendingRepository): Gauge = Gauge
        .builder("antall_innsendinger_stoppetopp") { innsendingRepository.antallStoppetUnderBehandling() }
        .description("gauge for å telle antall innsendinger i db som har stoppet opp under behandling")
        .register(Metrics.globalRegistry)
}
