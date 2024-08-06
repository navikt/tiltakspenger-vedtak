package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class TiltakDeltagelseSaksopplysningDbJson(
    val tiltakNavn: String,
    val tidsstempel: String,
    val deltagelsePeriode: PeriodeDbJson,
    val girRett: Boolean,
    val status: String,
    val kilde: String,
) {
    fun toDomain(): TiltakDeltagelseSaksopplysning {
        return TiltakDeltagelseSaksopplysning.Register(
            tiltakNavn = tiltakNavn,
            tidsstempel = LocalDateTime.parse(tidsstempel),
            deltagelsePeriode = deltagelsePeriode.toDomain(),
            girRett = girRett,
            // TODO tiltak jah: girRett er Komet sin avgjørelse. Vi bør lagre vår egen også.
            status = status.toTiltakDeltakerstatus(),
            kilde = kilde.toTiltakskilde(),
        )
    }
}

internal fun TiltakDeltagelseSaksopplysning.toDbJson(): TiltakDeltagelseSaksopplysningDbJson {
    return TiltakDeltagelseSaksopplysningDbJson(
        tiltakNavn = tiltakNavn,
        tidsstempel = tidsstempel.toString(),
        deltagelsePeriode = deltagelsePeriode.toDbJson(),
        girRett = girRett,
        status = status.toDb(),
        kilde = kilde.toDb(),
    )
}
