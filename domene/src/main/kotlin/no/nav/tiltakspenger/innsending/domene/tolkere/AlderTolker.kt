package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import java.time.LocalDate

// TODO: Denne vil jeg skrive om, vi burde ikke vilkårsvurdere når vi mottar dataene.
class AlderTolker {
    companion object {
        fun tolkeData(fdato: LocalDate, vurderingsperiode: Periode): YtelseSaksopplysning =
            fdato.plusYears(18).let {
                if (vurderingsperiode.inneholder(it)) {
                    YtelseSaksopplysning(
                        kilde = Kilde.PDL,
                        vilkår = Inngangsvilkår.ALDER,
                        detaljer = "",
                        saksbehandler = null,
                        harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                            .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, vurderingsperiode)
                            .setVerdiForDelPeriode(
                                HarYtelse.HAR_IKKE_YTELSE,
                                Periode(it, vurderingsperiode.til),
                            ),
                    )
                } else {
                    if (vurderingsperiode.før(it)) {
                        YtelseSaksopplysning(
                            kilde = Kilde.PDL,
                            vilkår = Inngangsvilkår.ALDER,
                            detaljer = "",
                            saksbehandler = null,
                            harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                                .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, vurderingsperiode),
                        )
                    } else {
                        YtelseSaksopplysning(
                            kilde = Kilde.PDL,
                            vilkår = Inngangsvilkår.ALDER,
                            detaljer = "",
                            saksbehandler = null,
                            harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                                .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                        )
                    }
                }
            }
    }
}
