package no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import java.time.LocalDateTime

fun Søknad.introSaksopplysning(vurderingsperiode: Periode): IntroSaksopplysning {
    return when (intro) {
        is Søknad.PeriodeSpm.Nei -> IntroSaksopplysning.Søknad(
            deltar = Periodisering(
                listOf(PeriodeMedVerdi(Deltagelse.DELTAR_IKKE, vurderingsperiode)),
            ),
            tidsstempel = LocalDateTime.now(),
        )

        is Søknad.PeriodeSpm.Ja -> {
            require(vurderingsperiode.inneholderHele(intro.periode)) {
                "Vurderingens periode ($vurderingsperiode) må inneholde hele perioden fra søknaden (${intro.periode})."
            }
            IntroSaksopplysning.Søknad(
                deltar = Periodisering(Deltagelse.DELTAR_IKKE, vurderingsperiode).setVerdiForDelPeriode(
                    Deltagelse.DELTAR,
                    intro.periode,
                ),
                tidsstempel = LocalDateTime.now(),
            )
        }
    }
}
