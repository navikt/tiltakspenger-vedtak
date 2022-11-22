package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.SøkerId
import java.time.LocalDateTime

class Søker(
    val søkerId: SøkerId,
    val ident: String,
    val tidsstempel: LocalDateTime,
    val opprettet: LocalDateTime,
)
