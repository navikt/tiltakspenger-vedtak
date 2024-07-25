package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.tiltakSaksopplysning(): TiltakSaksopplysning {
    return TiltakSaksopplysning.Søknad(tiltak = LocalDateTime.now(), tidsstempel = LocalDateTime.now())
}
