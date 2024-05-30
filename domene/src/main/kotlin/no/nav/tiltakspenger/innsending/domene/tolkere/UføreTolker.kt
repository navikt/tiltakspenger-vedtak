package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.innsending.domene.UføreVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

class UføreTolker {
    companion object {
        fun tolkeData(vedtak: UføreVedtak, vurderingsperiode: Periode): LivoppholdSaksopplysning {
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
                return LivoppholdSaksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = Vilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelse = Periodisering<HarYtelse?>(null, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                )
            }

            // Vedtak om uførevedtak skjer et sted i vår periode
            if (vurderingsperiode.inneholder(dato)) {
                return LivoppholdSaksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = Vilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelse = Periodisering<HarYtelse?>(null, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            Periode(dato, vurderingsperiode.til),
                        ),
                )
            }

            // Vedtak om uførevedtak skjer før vår periode
            return LivoppholdSaksopplysning(
                kilde = Kilde.PESYS,
                vilkår = Vilkår.UFØRETRYGD,
                detaljer = "",
                saksbehandler = null,
                harYtelse = Periodisering<HarYtelse?>(null, vurderingsperiode)
                    .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, vurderingsperiode),
            )
        }
    }
}
