package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.tiltak.toDb
import no.nav.tiltakspenger.vedtak.repository.tiltak.toTiltakstypeSomGirRett
import java.time.LocalDateTime

internal data class TiltakDeltagelseSaksopplysningDbJson(
    val tiltakNavn: String,
    val eksternTiltakId: String,
    val gjennomføringId: String?,
    val tidsstempel: String,
    val deltagelsePeriode: PeriodeDbJson,
    val girRett: Boolean,
    val status: String,
    val kilde: String,
    val tiltakstype: String,
) {
    fun toDomain(): TiltakDeltagelseSaksopplysning =
        TiltakDeltagelseSaksopplysning.Register(
            tiltakNavn = tiltakNavn,
            eksternTiltakId = eksternTiltakId,
            gjennomføringId = gjennomføringId,
            tidsstempel = LocalDateTime.parse(tidsstempel),
            deltagelsePeriode = deltagelsePeriode.toDomain(),
            girRett = girRett,
            status = status.toTiltakDeltakerstatus(),
            kilde = kilde.toTiltakskilde(),
            tiltakstype = tiltakstype.toTiltakstypeSomGirRett(),
        )
}

internal fun TiltakDeltagelseSaksopplysning.toDbJson(): TiltakDeltagelseSaksopplysningDbJson =
    TiltakDeltagelseSaksopplysningDbJson(
        tiltakNavn = tiltakNavn,
        eksternTiltakId = eksternTiltakId,
        gjennomføringId = gjennomføringId,
        tidsstempel = tidsstempel.toString(),
        deltagelsePeriode = deltagelsePeriode.toDbJson(),
        girRett = girRett,
        status = status.toDb(),
        kilde = kilde.toDb(),
        tiltakstype = tiltakstype.toDb(),
    )
