package no.nav.tiltakspenger.domene

import java.time.LocalDate

class Søknad(
    id: String,
    fnr: String,
    periode: Periode,
    innsendtdato: LocalDate,
    tiltak: String,
    deltarKvp: Boolean,
)
