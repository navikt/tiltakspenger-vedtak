package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.kravfristSaksopplysning(vurderingsperiode: Periode): KravfristSaksopplysning {
    val kravdato = tidsstempelHosOss
    return KravfristSaksopplysning.Søknad(kravdato = kravdato, vurderingsperiode = vurderingsperiode, tidsstempel = LocalDateTime.now())
}
