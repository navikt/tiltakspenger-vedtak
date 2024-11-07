package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDateTime

fun Tiltak.tilRegisterSaksopplysning(): TiltakDeltagelseSaksopplysning.Register =
    TiltakDeltagelseSaksopplysning.Register(
        tiltakNavn = this.gjennomføring.typeNavn,
        deltagelsePeriode = this.deltakelsesperiode,
        kilde = this.kilde,
        status = this.deltakelseStatus,
        girRett = this.gjennomføring.rettPåTiltakspenger,
        tidsstempel = LocalDateTime.now(),
        tiltakstype = this.gjennomføring.typeKode,
        eksternId = this.eksternId,
    )
