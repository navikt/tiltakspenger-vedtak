package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

fun Søknad.kravfristSaksopplysning(): KravfristSaksopplysning.Søknad =
    KravfristSaksopplysning.Søknad(kravdato = tidsstempelHosOss, tidsstempel = nå())
