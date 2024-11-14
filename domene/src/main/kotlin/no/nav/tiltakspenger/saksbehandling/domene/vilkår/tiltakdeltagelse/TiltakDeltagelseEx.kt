package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak

fun Tiltak.tilRegisterSaksopplysning(): TiltakDeltagelseSaksopplysning.Register =
    TiltakDeltagelseSaksopplysning.Register(
        tiltaksnavn = this.typeNavn,
        eksternTiltakId = this.eksternId,
        gjennomføringId = this.gjennomføringId,
        deltagelsePeriode = this.deltakelsesperiode,
        kilde = this.kilde,
        status = this.deltakelseStatus,
        girRett = this.rettPåTiltakspenger,
        tidsstempel = nå(),
        tiltakstype = this.typeKode,
    )
