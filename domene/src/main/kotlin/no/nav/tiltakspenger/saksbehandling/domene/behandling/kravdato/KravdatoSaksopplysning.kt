package no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate

data class KravdatoSaksopplysning(
    val kravdato: LocalDate,
    val kilde: Kilde,
    val saksbehandlerIdent: String? = null,
)
