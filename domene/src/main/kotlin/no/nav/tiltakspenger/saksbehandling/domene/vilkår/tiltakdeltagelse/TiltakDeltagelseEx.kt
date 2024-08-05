package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDateTime

fun Tiltak.tilRegisterSaksopplysning(): TiltakDeltagelseSaksopplysning.Register {
    return TiltakDeltagelseSaksopplysning.Register(
        tiltakNavn = gjennomføring.typeNavn,
        deltagelsePeriode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom),
        kilde = kilde,
        status = deltakelseStatus,
        girRett = gjennomføring.rettPåTiltakspenger,
        tidsstempel = LocalDateTime.now(),
    )
}
