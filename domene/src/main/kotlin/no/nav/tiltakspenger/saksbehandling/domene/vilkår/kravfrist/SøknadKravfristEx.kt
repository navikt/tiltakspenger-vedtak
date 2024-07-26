package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.kravfristSaksopplysning(): KravfristSaksopplysning {
    val kravdato = tidsstempelHosOss
    return KravfristSaksopplysning.Søknad(kravdato = kravdato, tidsstempel = LocalDateTime.now())
}
