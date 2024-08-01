package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDateTime

fun Tiltak.tiltakSaksopplysning(): TiltakDeltagelseSaksopplysning {
    return TiltakDeltagelseSaksopplysning.Tiltak(
        tiltakNavn = gjennomføring.typeNavn,
        deltagelsePeriode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom),
        kilde = kilde,
        status = deltakelseStatus,
        girRett = gjennomføring.rettPåTiltakspenger,
        tidsstempel = LocalDateTime.now(),
    )
}
