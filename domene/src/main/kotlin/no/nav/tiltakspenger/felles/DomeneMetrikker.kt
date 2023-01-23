package no.nav.tiltakspenger.felles

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet

object DomeneMetrikker {

    fun utfasCounter(tiltakstype: Tiltaksaktivitet.Tiltak): Counter = Counter
        .builder("antall_utfas")
        .tags("tiltak", tiltakstype.name)
        .description("counter for utfasede tiltak bruker søker om tiltakspenger for")
        .register(Metrics.globalRegistry)

    fun søknadMottattCounter(): Counter = Counter
        .builder("antall_søknader_mottatt")
        .description("counter for mottatte søknader")
        .register(Metrics.globalRegistry)
}
