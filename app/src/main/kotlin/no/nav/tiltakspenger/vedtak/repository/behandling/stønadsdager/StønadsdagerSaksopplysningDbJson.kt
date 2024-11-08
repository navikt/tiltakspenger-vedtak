package no.nav.tiltakspenger.vedtak.repository.behandling.stønadsdager

import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.StønadsdagerSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse.toDb
import no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse.toTiltakskilde
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.time.LocalDateTime

internal data class StønadsdagerSaksopplysningDbJson(
    val tiltakNavn: String,
    val eksternTiltakId: String,
    val gjennomføringId: String?,
    val antallDager: Int,
    val periode: PeriodeDbJson,
    val tidsstempel: String,
    val kilde: String,
) {
    fun toDomain(): StønadsdagerSaksopplysning =
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = tiltakNavn,
            eksternTiltakId = eksternTiltakId,
            gjennomføringId = gjennomføringId,
            antallDager = antallDager,
            tidsstempel = LocalDateTime.parse(tidsstempel),
            periode = periode.toDomain(),
            kilde = kilde.toTiltakskilde(),
        )
}

internal fun StønadsdagerSaksopplysning.toDbJson(): StønadsdagerSaksopplysningDbJson =
    StønadsdagerSaksopplysningDbJson(
        tiltakNavn = tiltakNavn,
        eksternTiltakId = eksternTiltakId,
        gjennomføringId = gjennomføringId,
        antallDager = antallDager,
        tidsstempel = tidsstempel.toString(),
        periode = periode.toDbJson(),
        kilde = kilde.toDb(),
    )
