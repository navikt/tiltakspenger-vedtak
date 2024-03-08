package no.nav.tiltakspenger.innsending

import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
import java.time.LocalDateTime

data class InnhentedePersonopplysninger(
    val personopplysningerliste: List<Personopplysninger>,
    val tidsstempelInnhentet: LocalDateTime,
    val tidsstempelSkjermingInnhentet: LocalDateTime? = null,
)
