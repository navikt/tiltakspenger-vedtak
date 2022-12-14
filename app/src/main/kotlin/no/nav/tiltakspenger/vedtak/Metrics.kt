package no.nav.tiltakspenger.vedtak

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics
import java.util.function.Supplier

// Denne fungerer, den dukker nå opp på /metrics-siden
fun noeGauge(noeSupplier: Supplier<Number>): Gauge = Gauge
    .builder("antall_noe", noeSupplier)
    .description("gauge for å telle antall noe")
    .register(Metrics.globalRegistry)
