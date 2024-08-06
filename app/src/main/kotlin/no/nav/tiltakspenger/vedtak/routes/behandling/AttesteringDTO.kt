package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.AttesteringStatus
import java.time.LocalDateTime

data class AttesteringDTO(
    val endretAv: String,
    val status: AttesteringStatus,
    val begrunnelse: String?,
    val endretTidspunkt: LocalDateTime,
)

/*
internal fun Attesteringer.toDTO(): List<AttesteringDTO> {
    return this.map { it.toDTO() }
}*/

internal fun Attestering.toDTO() = AttesteringDTO(
    endretAv = this.beslutter,
    status = this.svar,
    begrunnelse = this.begrunnelse,
    endretTidspunkt = this.tidspunkt,
)
