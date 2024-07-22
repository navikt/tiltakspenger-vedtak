package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import java.time.LocalDateTime

fun Søknad.kravdatoSaksopplysning(): KravdatoSaksopplysning {
    val kravdato = this.tidsstempelHosOss
    return KravdatoSaksopplysning.Søknad(kravdato = kravdato, tidsstempel = LocalDateTime.now())
}
