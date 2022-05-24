package no.nav.tiltakspenger.domene

import java.time.LocalDateTime

class Søknad(
    id: String,
    ident: String,
    opprettet: LocalDateTime,
    tiltak: Tiltak,
    deltarKvp: Boolean,
)
