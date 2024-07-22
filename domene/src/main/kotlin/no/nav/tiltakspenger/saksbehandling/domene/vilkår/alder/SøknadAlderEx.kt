package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDate
import java.time.LocalDateTime

fun Søknad.alderSaksopplysning(fødselsdato: LocalDate): AlderSaksopplysning {
    return AlderSaksopplysning.Søknad(fødselsdato = fødselsdato, tidsstempel = LocalDateTime.now())
}
