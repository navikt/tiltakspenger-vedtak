package no.nav.tiltakspenger.vedtak

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

object AppMetrikker {
    
    fun antallSøknaderLagret(innsendingRepository: InnsendingRepository): Gauge = Gauge
        .builder("antall_søknader_lagret") { innsendingRepository.antall() }
        .description("gauge for å telle antall søknader lagret i db")
        .register(Metrics.globalRegistry)

}
