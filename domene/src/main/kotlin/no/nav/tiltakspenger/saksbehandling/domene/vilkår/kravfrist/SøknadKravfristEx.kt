package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.kravfristSaksopplysning(): KravfristSaksopplysning.Søknad =
    KravfristSaksopplysning.Søknad(kravdato = tidsstempelHosOss, tidsstempel = LocalDateTime.now())
