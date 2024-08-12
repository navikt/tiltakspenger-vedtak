package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.AttesteringId
import java.time.LocalDateTime

data class Attestering(
    val id: AttesteringId = AttesteringId.random(),
    val status: Attesteringsstatus,
    val begrunnelse: String?,
    val beslutter: String,
    val tidspunkt: LocalDateTime = LocalDateTime.now(),
) {
    fun isGodkjent() = status == Attesteringsstatus.GODKJENT
}

enum class Attesteringsstatus {
    GODKJENT,
    SENDT_TILBAKE,
}
