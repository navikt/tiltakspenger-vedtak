package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak

fun Tiltak.tilRegisterSaksopplysning(): TiltaksdeltagelseSaksopplysning.Register =
    TiltaksdeltagelseSaksopplysning.Register(
        tiltaksnavn = this.typeNavn,
        eksternDeltagelseId = this.eksternDeltagelseId,
        gjennomføringId = this.gjennomføringId,
        deltagelsePeriode = this.deltakelsesperiode,
        kilde = this.kilde,
        status = this.deltakelseStatus,
        girRett = this.rettPåTiltakspenger,
        tidsstempel = nå(),
        tiltakstype = this.typeKode,
    )
