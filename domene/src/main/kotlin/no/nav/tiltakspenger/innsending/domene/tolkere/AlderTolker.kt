package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

// TODO: Denne vil jeg skrive om, vi burde ikke vilkårsvurdere når vi mottar dataene.
class AlderTolker {
    companion object {
        fun tolkeData(fdato: LocalDate, vurderingsperiode: Periode): Saksopplysning =
            fdato.plusYears(18).let {
                if (vurderingsperiode.inneholder(it)) {
                    Saksopplysning(
                        kilde = Kilde.PDL,
                        vilkår = Vilkår.ALDER,
                        detaljer = "",
                        saksbehandler = null,
                        harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                            .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_YTELSE, vurderingsperiode)
                            .setVerdiForDelPeriode(
                                HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
                                Periode(it, vurderingsperiode.til),
                            ),
                    )
                } else {
                    if (vurderingsperiode.før(it)) {
                        Saksopplysning(
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            saksbehandler = null,
                            harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                                .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_YTELSE, vurderingsperiode),
                        )
                    } else {
                        Saksopplysning(
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            saksbehandler = null,
                            harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                                .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_IKKE_YTELSE, vurderingsperiode),
                        )
                    }
                }
            }
    }
}
