package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.kravdatoSaksopplysning(vurderingsperiode: Periode): KravdatoSaksopplysning {
    val kravdato = tidsstempelHosOss
    return KravdatoSaksopplysning.Søknad(kravdato = kravdato, vurderingsperiode = vurderingsperiode, tidsstempel = LocalDateTime.now())
}
