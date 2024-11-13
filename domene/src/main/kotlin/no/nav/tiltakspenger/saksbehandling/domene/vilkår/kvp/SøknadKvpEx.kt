package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse

fun Søknad.kvpSaksopplysning(vurderingsperiode: Periode): KvpSaksopplysning =
    when (kvp) {
        is Søknad.PeriodeSpm.Nei ->
            KvpSaksopplysning.Søknad(
                deltar = Periodisering(
                    listOf(PeriodeMedVerdi(Deltagelse.DELTAR_IKKE, vurderingsperiode)),
                ),
                tidsstempel = nå(),
            )

        is Søknad.PeriodeSpm.Ja -> {
            require(vurderingsperiode.inneholderHele(kvp.periode)) {
                "Vurderingens periode ($vurderingsperiode) må inneholde hele perioden fra søknaden (${kvp.periode})."
            }
            KvpSaksopplysning.Søknad(
                deltar = Periodisering(Deltagelse.DELTAR_IKKE, vurderingsperiode).setVerdiForDelPeriode(
                    Deltagelse.DELTAR,
                    kvp.periode,
                ),
                tidsstempel = nå(),
            )
        }
    }
