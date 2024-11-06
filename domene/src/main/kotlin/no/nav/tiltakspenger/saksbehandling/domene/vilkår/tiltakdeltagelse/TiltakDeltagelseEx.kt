package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDateTime

fun Tiltak.tilRegisterSaksopplysning(): TiltakDeltagelseSaksopplysning.Register =
    TiltakDeltagelseSaksopplysning.Register(
        tiltakNavn = this.typeNavn,
        deltagelsePeriode = this.deltakelsesperiode,
        kilde = this.kilde,
        status = this.deltakelseStatus,
        girRett = this.rettPåTiltakspenger,
        tidsstempel = LocalDateTime.now(),
        tiltakstype = this.typeKode,
    )
