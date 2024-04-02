package no.nav.tiltakspenger.innsending.domene

import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import java.time.LocalDateTime

data class InnhentedePersonopplysninger(
    val personopplysningerliste: List<Personopplysninger>,
    val tidsstempelInnhentet: LocalDateTime,
    val tidsstempelSkjermingInnhentet: LocalDateTime? = null,
)
