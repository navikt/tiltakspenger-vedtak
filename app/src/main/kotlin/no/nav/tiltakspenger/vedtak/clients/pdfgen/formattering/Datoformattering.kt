package no.nav.tiltakspenger.vedtak.clients.pdfgen.formattering

import java.time.format.DateTimeFormatter
import java.util.Locale

val norskDatoFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(
        "d. MMMM yyyy",
        Locale
            .Builder()
            .setLanguage("no")
            .setRegion("NO")
            .build(),
    )
