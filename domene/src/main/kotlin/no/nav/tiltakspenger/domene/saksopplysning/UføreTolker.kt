package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.vedtak.UføreVedtak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår

class UføreTolker {
    companion object {
        fun tolkeData(vedtak: UføreVedtak, periode: Periode): List<Saksopplysning> {
            val dato = if (
                (!vedtak.harUføregrad) or
                (vedtak.virkDato == null)
            ) {
                31.desember(9999)
            } else {
                vedtak.virkDato!!
            }

            // har ikke uførevedtak eller det starter etter vår periode
            if (periode.før(dato)) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fra,
                        tom = periode.til,
                        kilde = Kilde.PESYS,
                        vilkår = Vilkår.UFØRETRYGD,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                        saksbehandler = null,
                    ),
                )
            }

            // Vedtak om uførevedtak skjer et sted i vår periode
            if (periode.inneholder(dato)) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fra,
                        tom = dato.minusDays(1),
                        kilde = Kilde.PESYS,
                        vilkår = Vilkår.UFØRETRYGD,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                        saksbehandler = null,
                    ),
                    Saksopplysning(
                        fom = dato,
                        tom = periode.til,
                        kilde = Kilde.PESYS,
                        vilkår = Vilkår.UFØRETRYGD,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                        saksbehandler = null,
                    ),
                )
            }

            // Vedtak om uførevedtak skjer før vår periode
            return listOf(
                Saksopplysning(
                    fom = periode.fra,
                    tom = periode.til,
                    kilde = Kilde.PESYS,
                    vilkår = Vilkår.UFØRETRYGD,
                    detaljer = "",
                    typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                    saksbehandler = null,
                ),
            )
        }
    }
}
