package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.innsending.domene.UføreVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

class UføreTolker {
    companion object {
        fun tolkeData(vedtak: UføreVedtak, vurderingsperiode: Periode): Saksopplysning {
            val dato = if (
                (!vedtak.harUføregrad) or
                (vedtak.virkDato == null)
            ) {
                31.desember(9999)
            } else {
                vedtak.virkDato!!
            }

            // har ikke uførevedtak eller det starter etter vår periode
            if (vurderingsperiode.før(dato)) {
                return Saksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = Vilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_IKKE_YTELSE, vurderingsperiode),
                )
            }

            // Vedtak om uførevedtak skjer et sted i vår periode
            if (vurderingsperiode.inneholder(dato)) {
                return Saksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = Vilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_IKKE_YTELSE, vurderingsperiode)
                        .setVerdiForDelPeriode(
                            HarYtelseSaksopplysning.HAR_YTELSE,
                            Periode(dato, vurderingsperiode.til),
                        ),
                )
            }

            // Vedtak om uførevedtak skjer før vår periode
            return Saksopplysning(
                kilde = Kilde.PESYS,
                vilkår = Vilkår.UFØRETRYGD,
                detaljer = "",
                saksbehandler = null,
                harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                    .setVerdiForDelPeriode(HarYtelseSaksopplysning.HAR_YTELSE, vurderingsperiode),
            )
        }
    }
}
