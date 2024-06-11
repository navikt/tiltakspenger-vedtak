package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.innsending.domene.UføreVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår

class UføreTolker {
    companion object {
        fun tolkeData(vedtak: UføreVedtak, vurderingsperiode: Periode): LivsoppholdYtelseSaksopplysning {
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
                return LivsoppholdYtelseSaksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = LivsoppholdDelVilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                )
            }

            // Vedtak om uførevedtak skjer et sted i vår periode
            if (vurderingsperiode.inneholder(dato)) {
                return LivsoppholdYtelseSaksopplysning(
                    kilde = Kilde.PESYS,
                    vilkår = LivsoppholdDelVilkår.UFØRETRYGD,
                    detaljer = "",
                    saksbehandler = null,
                    harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                        .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            Periode(dato, vurderingsperiode.til),
                        ),
                )
            }

            // Vedtak om uførevedtak skjer før vår periode
            return LivsoppholdYtelseSaksopplysning(
                kilde = Kilde.PESYS,
                vilkår = LivsoppholdDelVilkår.UFØRETRYGD,
                detaljer = "",
                saksbehandler = null,
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                    .setVerdiForDelPeriode(HarYtelse.HAR_YTELSE, vurderingsperiode),
            )
        }
    }
}
