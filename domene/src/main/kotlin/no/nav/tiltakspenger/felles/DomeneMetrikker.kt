package no.nav.tiltakspenger.felles

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics

object DomeneMetrikker {

    fun søknadMottattCounter(): Counter = Counter
        .builder("antall_søknader_mottatt")
        .description("counter for mottatte søknader")
        .register(Metrics.globalRegistry)
}
