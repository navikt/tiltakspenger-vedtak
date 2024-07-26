package no.nav.tiltakspenger.vedtak.repository.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class TiltakSaksopplysningDbJson(
    val tiltakNavn: String,
    val tidsstempel: String,
    val deltagelsePeriode: PeriodeDbJson,
    val girRett: Boolean,
    val status: String,
    val kilde: String,
) {
    fun toDomain(): TiltakDeltagelseSaksopplysning {
        return TiltakDeltagelseSaksopplysning.Tiltak(
            tiltakNavn = tiltakNavn,
            tidsstempel = LocalDateTime.parse(tidsstempel),
            deltagelsePeriode = deltagelsePeriode.toDomain(),
            girRett = girRett,
            status = status,
            kilde = kilde,
        )
    }
}

internal fun TiltakDeltagelseSaksopplysning.toDbJson(): TiltakSaksopplysningDbJson {
    return TiltakSaksopplysningDbJson(
        tiltakNavn = tiltakNavn,
        tidsstempel = tidsstempel.toString(),
        deltagelsePeriode = deltagelsePeriode.toDbJson(),
        girRett = girRett,
        status = status,
        kilde = kilde,
    )
}
