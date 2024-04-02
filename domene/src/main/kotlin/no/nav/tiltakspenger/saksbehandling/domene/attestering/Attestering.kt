package no.nav.tiltakspenger.saksbehandling.domene.attestering

import no.nav.tiltakspenger.felles.AttesteringId
import no.nav.tiltakspenger.felles.BehandlingId
import java.time.LocalDateTime

data class Attestering(
    val id: AttesteringId = AttesteringId.random(),
    val behandlingId: BehandlingId,
    val svar: AttesteringStatus,
    val begrunnelse: String?,
    val beslutter: String,
    val tidspunkt: LocalDateTime = LocalDateTime.now(),
)

enum class AttesteringStatus {
    GODKJENT,
    SENDT_TILBAKE,
}
