package no.nav.tiltakspenger.innsending.domene.tolkere
/*
import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.innsending.domene.UføreVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

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
                        fom = periode.fraOgMed,
                        tom = periode.tilOgMed,
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
                        fom = periode.fraOgMed,
                        tom = dato.minusDays(1),
                        kilde = Kilde.PESYS,
                        vilkår = Vilkår.UFØRETRYGD,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                        saksbehandler = null,
                    ),
                    Saksopplysning(
                        fom = dato,
                        tom = periode.tilOgMed,
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
                    fom = periode.fraOgMed,
                    tom = periode.tilOgMed,
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

 */
