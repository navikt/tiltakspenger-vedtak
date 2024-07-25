package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDateTime

fun Tiltak.tiltakSaksopplysning(): TiltakSaksopplysning {
    return TiltakSaksopplysning.Tiltak(tiltak = gjennomføring.typeNavn, deltagelsePeriode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom), kilde = kilde, status = deltakelseStatus.status, girRett = deltakelseStatus.rettTilÅSøke, tidsstempel = LocalDateTime.now())
}
