package no.nav.tiltakspenger.vedtak.repository.behandling.felles

import no.nav.tiltakspenger.felles.AttesteringId
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.AttesteringStatus
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import java.security.InvalidParameterException
import java.time.LocalDateTime

/**
 * Har ansvar for å serialisere/deserialisere Attesteringer til og fra json for lagring i database.
 */
private class AttesteringJson(
    val id: AttesteringId,
    val behandlingId: BehandlingId,
    val svar: AttesteringStatus,
    val begrunnelse: String?,
    val beslutter: String,
    val tidspunkt: LocalDateTime,
)

internal fun String.toAttesteringer(): List<Attestering> {
    try {
        val attesteringerJson = deserialize<List<AttesteringJson>>(this)
        return attesteringerJson.map {
            Attestering(
                id = it.id,
                behandlingId = it.behandlingId,
                svar = it.svar,
                begrunnelse = it.begrunnelse,
                beslutter = it.beslutter,
                tidspunkt = it.tidspunkt,
            )
        }
    } catch (exception: Exception) {
        throw InvalidParameterException("Det oppstod en feil ved parsing av json: " + exception.message)
    }
}

internal fun List<Attestering>.toDbJson(): String {
    return serialize(
        this.map {
            AttesteringJson(
                id = it.id,
                behandlingId = it.behandlingId,
                svar = it.svar,
                begrunnelse = it.begrunnelse,
                beslutter = it.beslutter,
                tidspunkt = it.tidspunkt,
            )
        },
    )
}
